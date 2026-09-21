package com.dclab.service;

import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class SystemLogService {
    private final CopyOnWriteArrayList<String> logs = new CopyOnWriteArrayList<>();
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");

    public void log(String category, String message) {
        String logEntry = String.format("[%s] %s: %s", LocalTime.now().format(formatter), category, message);
        logs.add(logEntry);
    }

    public List<String> getLogs() {
        return logs;
    }

    public List<String> getLogsSince(int fromIndex) {
        if (fromIndex < 0 || fromIndex >= logs.size()) {
            return List.of();
        }
        return logs.subList(fromIndex, logs.size());
    }

    public void clear() {
        logs.clear();
    }
}
