package com.example.monitorapp;

import android.widget.Switch;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class UserThreshold implements Serializable {
    enum IndexName {
        TEMP(0), HUMID(1), GAS(2);

        private int value;

        public int getValue() {
            return value;
        }

        public void setValue(int value) {
            this.value = value;
        }
        private IndexName(int value) {
            this.value = value;
        }
    }
    public IndexThreshold[] listThreshold;
    private String message;
    private String ID;

    public IndexThreshold[] getListThreshold() {
        return listThreshold;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public void setListThreshold(IndexThreshold[] listThreshold) {
        this.listThreshold = listThreshold;
    }

    public UserThreshold(){
        listThreshold = new IndexThreshold[IndexName.values().length];
        message="";
    }

    @Override
    public String toString() {
        String res = ID + ": " + message + " (";
        for(int i=0;i<IndexName.values().length; i++)
        {
            if (listThreshold[i].isUse()) {
                res += listThreshold[i].toString();
                switch (i) {
                    case 0:
                        res += " độ C";
                        break;
                    case 1:
                        res += "%";
                        break;
                    case 2:
                        res += "ppm";
                        break;
                    default:
                }
                if (!(i+1==IndexName.values().length || !listThreshold[i+1].isUse()))
                    res+=", ";
            }
        }
        res+=")";
        return res;
    }
}
