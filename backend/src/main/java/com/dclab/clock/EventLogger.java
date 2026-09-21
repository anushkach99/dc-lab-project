package com.dclab.clock;

import com.dclab.model.LamportEvent;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;
import java.util.Comparator;

@Component
public class EventLogger {
    private final CopyOnWriteArrayList<LamportEvent> eventLog = new CopyOnWriteArrayList<>();

    public void logEvent(String nodeId, String eventType, long lamportTimestamp, String taskId, String description) {
        LamportEvent event = new LamportEvent(nodeId, eventType, lamportTimestamp, System.currentTimeMillis(), taskId, description);
        eventLog.add(event);
    }

    public List<LamportEvent> getEvents() {
        return eventLog.stream()
                .sorted(Comparator.comparingLong(LamportEvent::getLamportTimestamp))
                .collect(Collectors.toList());
    }

    public List<LamportEvent> getEventsSince(long timestamp) {
        return eventLog.stream()
                .filter(e -> e.getRealTimestamp() > timestamp)
                .sorted(Comparator.comparingLong(LamportEvent::getLamportTimestamp))
                .collect(Collectors.toList());
    }

    public void clear() {
        eventLog.clear();
    }
}
