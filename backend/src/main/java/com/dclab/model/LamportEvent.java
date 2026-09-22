package com.dclab.model;

import java.io.Serializable;

public class LamportEvent implements Serializable {
    private String nodeId;
    private String eventType;
    private long lamportTimestamp;
    private long realTimestamp;
    private String taskId;
    private String description;

    public LamportEvent() {}

    public LamportEvent(String nodeId, String eventType, long lamportTimestamp, long realTimestamp, String taskId, String description) {
        this.nodeId = nodeId;
        this.eventType = eventType;
        this.lamportTimestamp = lamportTimestamp;
        this.realTimestamp = realTimestamp;
        this.taskId = taskId;
        this.description = description;
    }

    public String getNodeId() { return nodeId; }
    public void setNodeId(String nodeId) { this.nodeId = nodeId; }
    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }
    public long getLamportTimestamp() { return lamportTimestamp; }
    public void setLamportTimestamp(long lamportTimestamp) { this.lamportTimestamp = lamportTimestamp; }
    public long getRealTimestamp() { return realTimestamp; }
    public void setRealTimestamp(long realTimestamp) { this.realTimestamp = realTimestamp; }
    public String getTaskId() { return taskId; }
    public void setTaskId(String taskId) { this.taskId = taskId; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    // Frontend alias
    public long getTimestamp() { return realTimestamp; }
}
