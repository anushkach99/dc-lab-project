package com.dclab.model;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class SchedulerState implements Serializable {
    private Map<String, WorkerInfo> workerRegistry;
    private List<Job> jobs;
    private List<Task> tasks;
    private Map<String, String> taskAssignments;
    private String coordinatorId;
    private int coordinatorNodeId;
    private long version;
    private long timestamp;

    public SchedulerState() {}

    public SchedulerState(Map<String, WorkerInfo> workerRegistry, List<Job> jobs, List<Task> tasks, Map<String, String> taskAssignments, String coordinatorId, int coordinatorNodeId, long version, long timestamp) {
        this.workerRegistry = workerRegistry;
        this.jobs = jobs;
        this.tasks = tasks;
        this.taskAssignments = taskAssignments;
        this.coordinatorId = coordinatorId;
        this.coordinatorNodeId = coordinatorNodeId;
        this.version = version;
        this.timestamp = timestamp;
    }

    public Map<String, WorkerInfo> getWorkerRegistry() { return workerRegistry; }
    public void setWorkerRegistry(Map<String, WorkerInfo> workerRegistry) { this.workerRegistry = workerRegistry; }
    public List<Job> getJobs() { return jobs; }
    public void setJobs(List<Job> jobs) { this.jobs = jobs; }
    public List<Task> getTasks() { return tasks; }
    public void setTasks(List<Task> tasks) { this.tasks = tasks; }
    public Map<String, String> getTaskAssignments() { return taskAssignments; }
    public void setTaskAssignments(Map<String, String> taskAssignments) { this.taskAssignments = taskAssignments; }
    public String getCoordinatorId() { return coordinatorId; }
    public void setCoordinatorId(String coordinatorId) { this.coordinatorId = coordinatorId; }
    public int getCoordinatorNodeId() { return coordinatorNodeId; }
    public void setCoordinatorNodeId(int coordinatorNodeId) { this.coordinatorNodeId = coordinatorNodeId; }
    public long getVersion() { return version; }
    public void setVersion(long version) { this.version = version; }
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}
