package com.example.monitorapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.FirebaseApp;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class MainActivity2 extends AppCompatActivity implements SeekBar.OnSeekBarChangeListener {
    private SeekBar skbTemp;
    private EditText edtTemp;
    private CheckBox ckbTemp;
    private CheckBox ckbTempGreater;
    private SeekBar skbHumid;
    private EditText edtHumid;
    private CheckBox ckbHumid;
    private CheckBox ckbHumidGreater;
    private SeekBar skbGas;
    private EditText edtGas;
    private CheckBox ckbGas;
    private CheckBox ckbGasGreater;
    private EditText edtMessage;
    private Button btnHome;
    public static transient IndexThreshold indexThreshold = null;
    private TextView tvMessage;
    private ListView lvMessage;
    private Button btSave;
    private ArrayList<UserThreshold> lstUserThresh; //Phần cài đặt của user
    private ArrayList<String> arrMess; //Hiển thị lên listview

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        lstUserThresh = new ArrayList<UserThreshold>();
        arrMess = new ArrayList<String>();

        GetIDView();
        skbHumid.setOnSeekBarChangeListener(this);
        skbTemp.setOnSeekBarChangeListener(this);
        skbGas.setOnSeekBarChangeListener(this);

        ArrayAdapter<String> adapter;
        adapter = new ArrayAdapter<String>(MainActivity2.this, android.R.layout.simple_list_item_1, arrMess);
        lvMessage.setAdapter(adapter);

        edtTemp.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                return false;
            }
        });

        btSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!CheckInput())
                {
                    Toast.makeText(MainActivity2.this, "Kiểm tra nhập thông tin!", Toast.LENGTH_SHORT).show();
                    return;
                }
                InputUserThreshold();
                arrMess.add(lstUserThresh.get(lstUserThresh.size()-1).toString());
                adapter.notifyDataSetChanged();
                TextView label = (TextView) findViewById(R.id.tvLabel);
                label.setText("Các ngưỡng đã lưu: (" + arrMess.size() + ")");
                //SEND AND RECEIVE LIST USERTHRESHOLD

            }
        });
        btnHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity2.this, MainActivity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable("lstuserThreshhold", lstUserThresh);
                intent.putExtra("sendUserThreshold", bundle);
                startActivity(intent);
            }
        });
    }


    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
        switch (seekBar.getId())
        {
            case R.id.skbTemp:
                edtTemp.setText(String.valueOf(i));
                break;
            case R.id.skbHumid:
                edtHumid.setText(String.valueOf(i));
                break;
            case R.id.skbGas:
                edtGas.setText(String.valueOf(i));
                break;
            default:
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    private boolean CheckInput(){
        int value=0;
        boolean temp, humid, gas;
        String str = "";
        if (ckbTemp.isChecked()){
            str = edtTemp.getText().toString().trim();
            if(str.length()==0)
                return false;
            value = Integer.valueOf(str);
            if (value<skbTemp.getMin() || value > skbTemp.getMax())
                return false;
        }
        if (ckbHumid.isChecked()){
            str = edtHumid.getText().toString().trim();
            if(str.length()==0)
                return false;
            value = Integer.valueOf(str);
            if (value<skbHumid.getMin() || value > skbHumid.getMax())
                return false;
        }
        if (ckbGas.isChecked()){
            str = edtGas.getText().toString().trim();
            if(str.length()==0)
                return false;
            value = Integer.valueOf(str);
            if (value<skbGas.getMin() || value > skbGas.getMax())
                return false;
        }

        boolean nochecked=!ckbGas.isChecked() && !ckbTemp.isChecked() && !ckbHumid.isChecked();
        str = edtMessage.getText().toString();
        return str.trim().length()>0 && !nochecked;
    }

    private void InputUserThreshold() {
        IndexThreshold temp, humid, gas;
        float value = 0;
        if (ckbTemp.isChecked()){
            value = Float.valueOf(edtTemp.getText().toString());
            temp = new IndexThreshold(value, ckbTempGreater.isChecked(), ckbTemp.isChecked());
        }
        else
            temp = new IndexThreshold();

        if (ckbHumid.isChecked()){
            value = Float.valueOf(edtHumid.getText().toString());
            humid = new IndexThreshold(value, ckbHumidGreater.isChecked(), ckbHumid.isChecked());
        }
        else
            humid = new IndexThreshold();

        if (ckbGas.isChecked()){
            value = Float.valueOf(edtGas.getText().toString());
            gas = new IndexThreshold(value, ckbGasGreater.isChecked(), ckbGas.isChecked());
        }
        else
            gas = new IndexThreshold();


        UserThreshold t = new UserThreshold();
        t.setListThreshold(new IndexThreshold[] {temp, humid, gas});
        t.setMessage(edtMessage.getText().toString());
        t.setID("user"+lstUserThresh.size());
        lstUserThresh.add(t);

    }

    private void GetIDView(){
        skbTemp = (SeekBar) findViewById(R.id.skbTemp);
        edtTemp = (EditText) findViewById(R.id.edtTemp);
        ckbTemp = (CheckBox) findViewById(R.id.ckbTemp);
        ckbTempGreater = (CheckBox) findViewById(R.id.ckbTempGreater);
        skbHumid = (SeekBar) findViewById(R.id.skbHumid);
        edtHumid = (EditText) findViewById(R.id.edtHumid);
        ckbHumid = (CheckBox) findViewById(R.id.ckbHumid);
        ckbHumidGreater = (CheckBox) findViewById(R.id.ckbHumidGreater);
        skbGas = (SeekBar) findViewById(R.id.skbGas);
        edtGas = (EditText) findViewById(R.id.edtGas);
        ckbGas = (CheckBox) findViewById(R.id.ckbGas);
        ckbGasGreater = (CheckBox) findViewById(R.id.ckbGasGreater);

        tvMessage = (TextView) findViewById(R.id.tvMessage);
        edtMessage = (EditText) findViewById(R.id.edtMessage);
        lvMessage = (ListView) findViewById(R.id.lvMessage);
        btSave = (Button) findViewById(R.id.btSave);
        btnHome = (Button) findViewById(R.id.idbtnHomeback);
        edtTemp.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                skbTemp.setOnSeekBarChangeListener(null);
                int value=0;
                int min, max;
                if (edtTemp.getText().toString().trim().length()==0){
                    return;
                }

                value = Integer.valueOf(edtTemp.getText().toString());
                min = skbTemp.getMin();
                max = skbTemp.getMax();
                if (value < min || value > max) {
                    Toast toast = Toast.makeText(MainActivity2.this, min + " <= value <= " + max, Toast.LENGTH_SHORT);
                    toast.show();
                }
                skbTemp.setProgress(value);
                skbTemp.setOnSeekBarChangeListener(MainActivity2.this);
            }
        });
        edtHumid.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                skbHumid.setOnSeekBarChangeListener(null);
                int value=0;
                int min, max;
                if (edtHumid.getText().toString().trim().length()==0){
                    return;
                }

                value = Integer.valueOf(edtHumid.getText().toString());
                min = skbHumid.getMin();
                max = skbHumid.getMax();
                if (value < min || value > max) {
                    Toast toast = Toast.makeText(MainActivity2.this, min + " <= value <= " + max, Toast.LENGTH_SHORT);
                    toast.show();
                }
                skbHumid.setProgress(value);
                skbHumid.setOnSeekBarChangeListener(MainActivity2.this);
            }
        });
        edtGas.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                skbGas.setOnSeekBarChangeListener(null);
                int value=0;
                int min, max;
                if (edtGas.getText().toString().trim().length()==0){
                    return;
                }

                value = Integer.valueOf(edtGas.getText().toString());
                min = skbGas.getMin();
                max = skbGas.getMax();
                if (value < min || value > max) {
                    Toast toast = Toast.makeText(MainActivity2.this, min + " <= value <= " + max, Toast.LENGTH_SHORT);
                    toast.show();
                }
                skbGas.setProgress(value);
                skbGas.setOnSeekBarChangeListener(MainActivity2.this);
            }
        });
    }
}