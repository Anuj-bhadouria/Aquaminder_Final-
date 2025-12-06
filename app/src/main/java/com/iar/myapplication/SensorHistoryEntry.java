package com.iar.myapplication;

public class SensorHistoryEntry {
    public double flowRate;
    public double totalVolume;
    public String status;
    public long timestamp;

    public SensorHistoryEntry() {}
    public SensorHistoryEntry(double flowRate, double totalVolume, String status, long timestamp) {
        this.flowRate = flowRate;
        this.totalVolume = totalVolume;
        this.status = status;
        this.timestamp = timestamp;
    }
}