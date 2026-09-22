package com.dclab.model;

import java.io.Serializable;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class LogEntry implements Serializable {
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    private long timestamp;
    private String category;
    private String message;

    public LogEntry() {}

    public LogEntry(String category, String message) {
        this.timestamp = System.currentTimeMillis();
        this.category = category;
        this.message = message;
    }

    public LogEntry(long timestamp, String category, String message) {
        this.timestamp = timestamp;
        this.category = category;
        this.message = message;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getFormattedTime() {
        return LocalTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault()).format(TIME_FORMATTER);
    }

    @Override
    public String toString() {
        return String.format("[%s] %s: %s", getFormattedTime(), category, message);
    }
}
