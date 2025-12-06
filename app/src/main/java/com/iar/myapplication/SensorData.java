package com.iar.myapplication; // Make sure this matches your package name

import java.util.HashMap;
import java.util.Map;

public class SensorData {
    public float flowRate;
    public float totalVolume;
    public String status;
    public long timestamp; // For the current reading
    public Map<String, HistoryEntry> history = new HashMap<>();

    public SensorData() {
        // Default constructor required for Firebase
    }

    public static class HistoryEntry {
        public float flowRate;
        public float totalVolume;
        public String status;
        public long timestamp;

        public HistoryEntry() {}

        public HistoryEntry(float flowRate, float totalVolume, String status, long timestamp) {
            this.flowRate = flowRate;
            this.totalVolume = totalVolume;
            this.status = status;
            this.timestamp = timestamp;
        }
    }
}
