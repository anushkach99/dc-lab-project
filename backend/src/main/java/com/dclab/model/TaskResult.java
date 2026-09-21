package com.dclab.model;

import java.io.Serializable;

public class TaskResult implements Serializable {
    private String taskId;
    private String workerId;
    private boolean success;
    private long executionTimeMs;
    private long lamportTimestamp;
    private String errorMessage;

    public TaskResult() {}

    public TaskResult(String taskId, String workerId, boolean success, long executionTimeMs, long lamportTimestamp, String errorMessage) {
        this.taskId = taskId;
        this.workerId = workerId;
        this.success = success;
        this.executionTimeMs = executionTimeMs;
        this.lamportTimestamp = lamportTimestamp;
        this.errorMessage = errorMessage;
    }

    public String getTaskId() { return taskId; }
    public void setTaskId(String taskId) { this.taskId = taskId; }
    public String getWorkerId() { return workerId; }
    public void setWorkerId(String workerId) { this.workerId = workerId; }
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    public long getExecutionTimeMs() { return executionTimeMs; }
    public void setExecutionTimeMs(long executionTimeMs) { this.executionTimeMs = executionTimeMs; }
    public long getLamportTimestamp() { return lamportTimestamp; }
    public void setLamportTimestamp(long lamportTimestamp) { this.lamportTimestamp = lamportTimestamp; }
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
}
