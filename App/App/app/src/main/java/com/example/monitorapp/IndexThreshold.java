package com.example.monitorapp;

import java.io.Serializable;

public class IndexThreshold implements Serializable {
    private float value;
    private boolean isGreater;
    private boolean isUse;

    public float getValue() {
        return value;
    }

    public IndexThreshold() {
        this.value = 0;
        this.isGreater = false;
        this.isUse = false;
    }

    public IndexThreshold(float value, boolean isGreater, boolean isUse) {
        this.value = value;
        this.isGreater = isGreater;
        this.isUse = isUse;
    }

    @Override
    public String toString() {
        String res="";
        if (isGreater)
            res+=">=";
        else
            res+="<";
        res+=value;
        return res;
    }

    public void setValue(float value) {
        this.value = value;
    }

    public boolean isGreater() {
        return isGreater;
    }

    public void setGreater(boolean greater) {
        isGreater = greater;
    }

    public boolean isUse() {
        return isUse;
    }

    public void setUse(boolean use) {
        isUse = use;
    }
}
