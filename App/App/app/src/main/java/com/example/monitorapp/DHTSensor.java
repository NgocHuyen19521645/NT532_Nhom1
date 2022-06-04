package com.example.monitorapp;

public class DHTSensor {
    private double Humidity;
    private double Temperature;
    private long Gas;
    private String Time;

    public DHTSensor(double humidity, double temperature, long gas, String timeStamp) {
        Humidity = humidity;
        Temperature = temperature;
        Gas = gas;
        Time = timeStamp;
    }

    public DHTSensor() {
    }

    public double getHumidity() {
        return Humidity;
    }

    public void setHumidity(double humidity) {
        Humidity = humidity;
    }

    public double getTemperature() {
        return Temperature;
    }

    public void setTemperature(double temperature) {
        Temperature = temperature;
    }

    public long getGas() {
        return Gas;
    }

    public void setGas(long gas) {
        Gas = gas;
    }

    public String getTime() {
        return Time;
    }

    public void setTime(String time) {
        Time = time;
    }
}
