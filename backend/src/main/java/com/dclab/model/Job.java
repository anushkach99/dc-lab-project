package com.dclab.model;

import java.io.Serializable;
import java.util.List;

public class Job implements Serializable {
    private String jobId;
    private int totalTasks;
    private int completedTasks;
    private int pendingTasks;
    private int failedTasks;
    private int runningTasks;
    private long startTime;
    private long endTime;
    private String status;
    private TaskComplexity complexity;
    private List<String> taskIds;

    public Job() {}

    public Job(String jobId, int totalTasks, int completedTasks, int pendingTasks, int failedTasks, int runningTasks, long startTime, long endTime, String status, TaskComplexity complexity, List<String> taskIds) {
        this.jobId = jobId;
        this.totalTasks = totalTasks;
        this.completedTasks = completedTasks;
        this.pendingTasks = pendingTasks;
        this.failedTasks = failedTasks;
        this.runningTasks = runningTasks;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
        this.complexity = complexity;
        this.taskIds = taskIds;
    }

    public String getJobId() { return jobId; }
    public void setJobId(String jobId) { this.jobId = jobId; }
    public int getTotalTasks() { return totalTasks; }
    public void setTotalTasks(int totalTasks) { this.totalTasks = totalTasks; }
    public int getCompletedTasks() { return completedTasks; }
    public void setCompletedTasks(int completedTasks) { this.completedTasks = completedTasks; }
    public int getPendingTasks() { return pendingTasks; }
    public void setPendingTasks(int pendingTasks) { this.pendingTasks = pendingTasks; }
    public int getFailedTasks() { return failedTasks; }
    public void setFailedTasks(int failedTasks) { this.failedTasks = failedTasks; }
    public int getRunningTasks() { return runningTasks; }
    public void setRunningTasks(int runningTasks) { this.runningTasks = runningTasks; }
    public long getStartTime() { return startTime; }
    public void setStartTime(long startTime) { this.startTime = startTime; }
    public long getEndTime() { return endTime; }
    public void setEndTime(long endTime) { this.endTime = endTime; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public TaskComplexity getComplexity() { return complexity; }
    public void setComplexity(TaskComplexity complexity) { this.complexity = complexity; }
    public List<String> getTaskIds() { return taskIds; }
    public void setTaskIds(List<String> taskIds) { this.taskIds = taskIds; }
}
