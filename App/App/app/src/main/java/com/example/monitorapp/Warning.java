package com.example.monitorapp;

public class Warning {
    private String timestamp;
    private String warningContent;

    public Warning(String timestamp, String warningContent) {
        this.timestamp = timestamp;
        this.warningContent = warningContent;
    }
    public Warning(){
        this.timestamp = "";
        this.warningContent = "";
    }
    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getWarningContent() {
        return warningContent;
    }

    public void setWarningContent(String warningContent) {
        this.warningContent = warningContent;
    }
}
