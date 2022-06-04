package com.example.monitorapp;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

public class WarningAdapter extends BaseAdapter {
    private ArrayList<Warning> warningList;
    private LayoutInflater layoutInflater;

    WarningAdapter(ArrayList<Warning> listWarning){
        this.warningList = listWarning;
    }

    @Override
    public int getCount() {
        return warningList.size();
    }

    @Override
    public Object getItem(int position) {
        return warningList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @SuppressLint("ResourceType")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View warning;
        if(convertView == null){
            warning = View.inflate(parent.getContext(), R.layout.item_warning, null);
        }
        else{
            warning = convertView;
        }
        Warning warning1 = (Warning)getItem(position);
        ((TextView) warning.findViewById(R.id.idtvTimestamp)).setText(warning1.getTimestamp());
        ((TextView)warning.findViewById(R.id.idtvWarningContent)).setText(warning1.getWarningContent());
        return warning;
    }
}
