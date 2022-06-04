package com.example.monitorapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.DataSet;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Vector;

public class MainActivity extends AppCompatActivity  implements OnChartValueSelectedListener{

    private TextView tvHumid;
    private  TextView tvTemp;
    private TextView tvGas;
    private ListView listView;
    private TextView selectedChart;
    private TextView icPrint;
    private TextView icSettings;
    private DatabaseReference reference;
    private Button btnClearWarning;
    private Spinner spinner;
    private ArrayList<UserThreshold> userThresholdArrayList;
    private ArrayList<Warning> warningArrayList;
    WarningAdapter warningAdapter;
    Warning warning;

    //chart
    private CombinedChart mChart;
    private TextView tv;
    private double realtimeData[];
    private int count;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this);
        setContentView(R.layout.activity_main);
        //
        FirebaseMessaging.getInstance().subscribeToTopic("weather")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        String msg = "Done";
                        if (!task.isSuccessful()) {
                            msg = "Fail";
                        }
                    }
                });

        //
        spinner = (Spinner) findViewById(R.id.spinner);
        tvHumid = (TextView) findViewById(R.id.idtvHumidValue);
        tvTemp = (TextView) findViewById(R.id.idtvTempValue);
        tvGas = (TextView) findViewById(R.id.idtvGasValue);
        icPrint = (TextView) findViewById(R.id.idicPrint);
        icSettings = (TextView) findViewById(R.id.idicSettings);
        listView = (ListView) findViewById(R.id.idlvWarning);
        btnClearWarning = (Button) findViewById(R.id.clearLV);

        userThresholdArrayList = new ArrayList<>();
        warningArrayList = new ArrayList<>();
        warningAdapter = new WarningAdapter(warningArrayList);
        ////

        ArrayList<String> chartString = new ArrayList<>();
        chartString.add("Nhiệt độ");
        chartString.add("Độ ẩm");
        chartString.add("Khí gas");
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.chart_spinner, chartString);
        this.spinner.setAdapter(adapter);
        String selectedChart = this.spinner.getSelectedItem().toString();

        // Hiển thị biểu đồ
        int numData = 30;
        realtimeData = new double[numData];
        count = 10;

        mChart = (CombinedChart) findViewById(R.id.chart);
        mChart.getDescription().setEnabled(false);
        mChart.setBackgroundColor(Color.WHITE);
        mChart.setDrawGridBackground(false);
        mChart.setDrawBarShadow(false);
        mChart.setHighlightFullBarEnabled(false);
        mChart.setOnChartValueSelectedListener(this);

        YAxis rightAxis = mChart.getAxisRight();
        rightAxis.setDrawGridLines(false);
        rightAxis.setAxisMinimum(0f);

        YAxis leftAxis = mChart.getAxisLeft();
        leftAxis.setDrawGridLines(false);
        leftAxis.setAxisMinimum(0f);

        final List<String> xLabel = new ArrayList<>();
        for (int i=count-1; i>=0; i--) {
            xLabel.add(String.valueOf(i));
        }
        XAxis xAxis = mChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setAxisMinimum(0f);
        xAxis.setGranularity(1f);
        xAxis.setValueFormatter(new com.github.mikephil.charting.formatter.IndexAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                return xLabel.get((int) value % xLabel.size());
            }
        });

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("DHTSensor");
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                String choice = spinner.getSelectedItem().toString();
                Query query = myRef.orderByKey().limitToLast(count);
                query.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        int i=0;
                        double maxValue=0;
                        double minValue=50;
                        if (choice=="Độ ẩm")
                        {
                            maxValue = 0;
                            minValue = 100;
                        }
                        else if (choice=="Khí gas")
                        {
                            maxValue = 0;
                            minValue = 40000;
                        }

                        for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                            DHTSensor element = snapshot.getValue(DHTSensor.class);
                            switch (choice)
                            {
                                case "Độ ẩm":
                                    realtimeData[i] = (float)element.getHumidity();
                                    break;
                                case "Khí gas":
                                    realtimeData[i] = (float)element.getGas();
                                    break;
                                default:
                                    realtimeData[i] = (float)element.getTemperature();
                            }
                            if (maxValue<realtimeData[i])
                                maxValue=realtimeData[i];
                            if (minValue>realtimeData[i])
                                minValue=realtimeData[i];
                            i++;
                        }
                        float var = (float)((maxValue-minValue)*0.5 + maxValue*0.01);
                        rightAxis.setAxisMaximum((float)maxValue+var);
                        rightAxis.setAxisMinimum((float)minValue-var);
                        leftAxis.setAxisMaximum((float)maxValue+var);
                        leftAxis.setAxisMinimum((float)minValue-var);
                        CombinedData data = new CombinedData();
                        LineData lineDatas = new LineData();
                        lineDatas.addDataSet((ILineDataSet) dataChart(realtimeData, count));

                        data.setData(lineDatas);

                        xAxis.setAxisMaximum(data.getXMax() + 0.25f);

                        mChart.setData(data);
                        mChart.invalidate();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        //

        reference = FirebaseDatabase.getInstance().getReference().child("DHTSensor");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Query lastQuery = reference.orderByKey().limitToLast(1);
                lastQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                            DHTSensor dhtSensor = snapshot.getValue(DHTSensor.class);
                            //getData from Firebase
                            double humid = dhtSensor.getHumidity();
                            double temp = dhtSensor.getTemperature();
                            long gas = dhtSensor.getGas();
                            String timestamp = dhtSensor.getTime();
                            //setext To Screen
                            tvHumid.setText((String.valueOf(humid)));
                            tvGas.setText(String.valueOf(gas));
                            tvTemp.setText(String.valueOf(temp));

                            if(temp > 35 ){
                                tvTemp.setTextColor(Color.RED);
                                Warning warning = new Warning(timestamp, "High Temperature");
                                warningArrayList.add(warning);
                                listView.setAdapter(warningAdapter);
                                warningAdapter.notifyDataSetChanged();
                                notification("Default", "High Temperature");
                            }
                            if(humid < 20){
                                tvTemp.setTextColor(Color.RED);
                                Warning warning = new Warning(timestamp, "Low Humid");
                                warningArrayList.add(warning);
                                listView.setAdapter(warningAdapter);
                                warningAdapter.notifyDataSetChanged();
                                notification("Default", "Low Humid");
                            }

                            if(gas > 40000){
                                tvTemp.setTextColor(Color.RED);
                                Warning warning = new Warning(timestamp, "High Gas");
                                warningArrayList.add(warning);
                                listView.setAdapter(warningAdapter);
                                warningAdapter.notifyDataSetChanged();
                                notification("Default", "High Gas");
                            }
                            //Receive Data From Activity2

                            Intent intent = getIntent();
                            Bundle bundle = intent.getBundleExtra("sendUserThreshold");
                            if(bundle != null){
                                userThresholdArrayList = (ArrayList<UserThreshold>) bundle.getSerializable("lstuserThreshhold");
                                for(int i=0; i < userThresholdArrayList.size(); i++){

                                    UserThreshold usrThreshold = userThresholdArrayList.get(i);
                                    boolean ketqua=true;
                                    if(usrThreshold.listThreshold[0].isUse())
                                    {
                                        boolean t = !((temp >=  usrThreshold.listThreshold[0].getValue())  ^ usrThreshold.listThreshold[0].isGreater());
                                        ketqua = ketqua && t;
                                    }
                                    if(usrThreshold.listThreshold[1].isUse())
                                    {
                                        boolean t = (humid >=  usrThreshold.listThreshold[1].getValue())  && usrThreshold.listThreshold[1].isGreater();
                                        ketqua = ketqua && t;
                                    }
                                    if(usrThreshold.listThreshold[2].isUse())
                                    {
                                        boolean t = (gas >=  usrThreshold.listThreshold[2].getValue())  && usrThreshold.listThreshold[2].isGreater();
                                        ketqua = ketqua && t;
                                    }
                                    if (ketqua==true)
                                    {
                                        tvTemp.setTextColor(Color.RED);
                                        Warning warning = new Warning(timestamp, usrThreshold.getMessage());
                                        warningArrayList.add(warning);
                                        listView.setAdapter(warningAdapter);
                                        warningAdapter.notifyDataSetChanged();
                                        notification(usrThreshold.getID(), usrThreshold.getMessage());
                                    }

//                                    if((temp >=  usrThreshold.listThreshold[0].getValue()  && usrThreshold.listThreshold[0].isGreater() && usrThreshold.listThreshold[0].isUse()) &&
//                                            ((humid >=  usrThreshold.listThreshold[1].getValue()  && usrThreshold.listThreshold[1].isGreater() && usrThreshold.listThreshold[1].isUse())) &&
//                                            ((gas >=  usrThreshold.listThreshold[2].getValue()  && usrThreshold.listThreshold[2].isGreater() && usrThreshold.listThreshold[2].isUse()))
//                                    ){
//                                        tvTemp.setTextColor(Color.RED);
//                                        Warning warning = new Warning(timestamp, usrThreshold.getMessage());
//                                        warningArrayList.add(warning);
//                                        listView.setAdapter(warningAdapter);
//                                        warningAdapter.notifyDataSetChanged();
//                                        notification(usrThreshold.getID(), usrThreshold.getMessage());
//                                    }
//                                    if((temp <  usrThreshold.listThreshold[0].getValue()  && (!usrThreshold.listThreshold[0].isGreater()) && usrThreshold.listThreshold[0].isUse()) &&
//                                            ((humid <  usrThreshold.listThreshold[1].getValue()  && (!usrThreshold.listThreshold[1].isGreater()) && usrThreshold.listThreshold[1].isUse())) &&
//                                            ((gas <  usrThreshold.listThreshold[2].getValue()  && (!usrThreshold.listThreshold[2].isGreater()) && usrThreshold.listThreshold[2].isUse()))
//                                    ){
//                                        tvTemp.setTextColor(Color.RED);
//                                        Warning warning = new Warning(timestamp, usrThreshold.getMessage());
//                                        warningArrayList.add(warning);
//                                        listView.setAdapter(warningAdapter);
//                                        warningAdapter.notifyDataSetChanged();
//                                        notification(usrThreshold.getID(), usrThreshold.getMessage());
//                                    }

                                }
                            }

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        icSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, MainActivity2.class);
                startActivity(intent);
            }
        });
        icPrint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takeScreenshot();
            }
        });
        btnClearWarning.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                warningArrayList.clear();
                listView.setAdapter(warningAdapter);
                warningAdapter.notifyDataSetChanged();
            }
        });
    }

    private void notification(String notiCode, String message){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel channel = new NotificationChannel("n", "n", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "n")
                .setSmallIcon(R.drawable.ic_baseline_print_24)
                .setContentTitle("NotiID: " + notiCode)
                .setContentText("Messsage: " + message)
                .setAutoCancel(true);
        NotificationManagerCompat managerCompat = NotificationManagerCompat.from(this);
        managerCompat.notify(999, builder.build());
    }

    private void takeScreenshot() {
        Date now = new Date();
        android.text.format.DateFormat.format("yyyy-MM-dd_hh:mm:ss", now);

        try {
            // image naming and path  to include sd card  appending name you choose for file
            String mPath = Environment.getExternalStorageDirectory().toString() + "/" + now + ".jpg";

            // create bitmap screen capture
            View v1 = getWindow().getDecorView().getRootView();
            v1.setDrawingCacheEnabled(true);
            Bitmap bitmap = Bitmap.createBitmap(v1.getDrawingCache());
            v1.setDrawingCacheEnabled(false);

            File imageFile = new File(mPath);

            FileOutputStream outputStream = new FileOutputStream(imageFile);
            int quality = 100;
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream);
            outputStream.flush();
            outputStream.close();

            openScreenshot(imageFile);
        } catch (Throwable e) {
            // Several error may come out with file handling or DOM
            e.printStackTrace();
        }
    }
    private void openScreenshot(File imageFile) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        Uri uri = Uri.fromFile(imageFile);
        intent.setDataAndType(uri, "image/*");
        startActivity(intent);
    }

    @Override
    public void onValueSelected(Entry e, Highlight h) {
        Toast.makeText(this, "Value: "
                + e.getY()
                + ", index: "
                + h.getX()
                + ", DataSet index: "
                + h.getDataSetIndex(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onNothingSelected() {

    }

    private static DataSet dataChart(double data[], int count) {
        LineData d = new LineData();
        //int[] data = new int[] { 1, 2, 2, 1, 1, 1, 2, 1, 1, 2, 1, 9 };
        ArrayList<Entry> entries = new ArrayList<Entry>();
        for (int index = 0; index < count; index++) {
            entries.add(new Entry(index, (float)data[index]));
        }

        LineDataSet set = new LineDataSet(entries, "Request Ots approved");
        set.setColor(Color.GREEN);
        set.setLineWidth(2.5f);
        set.setCircleColor(Color.GREEN);
        set.setCircleRadius(5f);
        set.setFillColor(Color.GREEN);
        set.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        set.setDrawValues(true);
        set.setValueTextSize(10f);
        set.setValueTextColor(Color.GREEN);

        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        d.addDataSet(set);

        return set;
    }

}