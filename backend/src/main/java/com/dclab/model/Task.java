package com.dclab.model;

import java.io.Serializable;

public class Task implements Serializable {
    private String taskId;
    private String jobId;
    private String assignedWorker;
    private TaskStatus status;
    private long startTime;
    private long endTime;
    private long executionTimeMs;
    private long lamportTimestamp;
    private TaskComplexity complexity;
    private int computationSize;
    private double progress;

    public Task() {}

    public Task(String taskId, String jobId, String assignedWorker, TaskStatus status, long startTime, long endTime, long executionTimeMs, long lamportTimestamp, TaskComplexity complexity, int computationSize, double progress) {
        this.taskId = taskId;
        this.jobId = jobId;
        this.assignedWorker = assignedWorker;
        this.status = status;
        this.startTime = startTime;
        this.endTime = endTime;
        this.executionTimeMs = executionTimeMs;
        this.lamportTimestamp = lamportTimestamp;
        this.complexity = complexity;
        this.computationSize = computationSize;
        this.progress = progress;
    }

    public String getTaskId() { return taskId; }
    public void setTaskId(String taskId) { this.taskId = taskId; }
    public String getJobId() { return jobId; }
    public void setJobId(String jobId) { this.jobId = jobId; }
    public String getAssignedWorker() { return assignedWorker; }
    public void setAssignedWorker(String assignedWorker) { this.assignedWorker = assignedWorker; }
    public TaskStatus getStatus() { return status; }
    public void setStatus(TaskStatus status) { this.status = status; }
    public long getStartTime() { return startTime; }
    public void setStartTime(long startTime) { this.startTime = startTime; }
    public long getEndTime() { return endTime; }
    public void setEndTime(long endTime) { this.endTime = endTime; }
    public long getExecutionTimeMs() { return executionTimeMs; }
    public void setExecutionTimeMs(long executionTimeMs) { this.executionTimeMs = executionTimeMs; }
    public long getLamportTimestamp() { return lamportTimestamp; }
    public void setLamportTimestamp(long lamportTimestamp) { this.lamportTimestamp = lamportTimestamp; }
    public TaskComplexity getComplexity() { return complexity; }
    public void setComplexity(TaskComplexity complexity) { this.complexity = complexity; }
    public int getComputationSize() { return computationSize; }
    public void setComputationSize(int computationSize) { this.computationSize = computationSize; }
    public double getProgress() { return progress; }
    public void setProgress(double progress) { this.progress = progress; }
}
