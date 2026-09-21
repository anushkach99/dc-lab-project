package com.dclab.model;

public class SystemStatus {
    private String coordinatorId;
    private int coordinatorNodeId;
    private int totalWorkers;
    private int onlineWorkers;
    private int activeJobs;
    private int completedTasks;
    private int pendingTasks;
    private int runningTasks;
    private int stragglerCount;
    private double avgThroughput;
    private String mode;
    private long uptime;
    private ReplicationState replicationState;

    public SystemStatus() {}

    public String getCoordinatorId() { return coordinatorId; }
    public void setCoordinatorId(String coordinatorId) { this.coordinatorId = coordinatorId; }
    public int getCoordinatorNodeId() { return coordinatorNodeId; }
    public void setCoordinatorNodeId(int coordinatorNodeId) { this.coordinatorNodeId = coordinatorNodeId; }
    public int getTotalWorkers() { return totalWorkers; }
    public void setTotalWorkers(int totalWorkers) { this.totalWorkers = totalWorkers; }
    public int getOnlineWorkers() { return onlineWorkers; }
    public void setOnlineWorkers(int onlineWorkers) { this.onlineWorkers = onlineWorkers; }
    public int getActiveJobs() { return activeJobs; }
    public void setActiveJobs(int activeJobs) { this.activeJobs = activeJobs; }
    public int getCompletedTasks() { return completedTasks; }
    public void setCompletedTasks(int completedTasks) { this.completedTasks = completedTasks; }
    public int getPendingTasks() { return pendingTasks; }
    public void setPendingTasks(int pendingTasks) { this.pendingTasks = pendingTasks; }
    public int getRunningTasks() { return runningTasks; }
    public void setRunningTasks(int runningTasks) { this.runningTasks = runningTasks; }
    public int getStragglerCount() { return stragglerCount; }
    public void setStragglerCount(int stragglerCount) { this.stragglerCount = stragglerCount; }
    public double getAvgThroughput() { return avgThroughput; }
    public void setAvgThroughput(double avgThroughput) { this.avgThroughput = avgThroughput; }
    public String getMode() { return mode; }
    public void setMode(String mode) { this.mode = mode; }
    public long getUptime() { return uptime; }
    public void setUptime(long uptime) { this.uptime = uptime; }
    public ReplicationState getReplicationState() { return replicationState; }
    public void setReplicationState(ReplicationState replicationState) { this.replicationState = replicationState; }
}
