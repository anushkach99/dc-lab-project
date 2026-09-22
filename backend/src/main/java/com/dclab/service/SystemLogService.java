package com.dclab.service;

import com.dclab.model.LogEntry;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class SystemLogService {
    private final CopyOnWriteArrayList<LogEntry> logs = new CopyOnWriteArrayList<>();

    public void log(String category, String message) {
        LogEntry entry = new LogEntry(category, message);
        logs.add(entry);
        System.out.println(entry.toString());
    }

    public void addLog(String category, String message) {
        log(category, message);
    }

    public List<LogEntry> getLogs() {
        return logs;
    }

    public List<LogEntry> getLogsSince(int fromIndex) {
        if (fromIndex < 0 || fromIndex >= logs.size()) {
            return List.of();
        }
        return logs.subList(fromIndex, logs.size());
    }

    public void clear() {
        logs.clear();
    }
}
