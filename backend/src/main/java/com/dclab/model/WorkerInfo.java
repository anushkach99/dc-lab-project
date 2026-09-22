package com.dclab.model;

import java.io.Serializable;

public class WorkerInfo implements Serializable {
    private String workerId;
    private String ip;
    private String hostname;
    private NodeType nodeType;
    private int cpuCores;
    private long ramMB;
    private double cpuFrequencyGHz;
    private double cpuUtilization;
    private NodeStatus status;
    private int nodeId;
    private double throughput;
    private double avgTaskTime;
    private long lamportClock;
    private int activeTasks;
    private int completedTasks;
    private int pendingTasks;
    private long lastHeartbeat;
    private double simulatedSpeed = 1.0;

    public WorkerInfo() {}

    public WorkerInfo(String workerId, String ip, String hostname, NodeType nodeType, int cpuCores, long ramMB, double cpuFrequencyGHz, double cpuUtilization, NodeStatus status, int nodeId, double throughput, double avgTaskTime, long lamportClock, int activeTasks, int completedTasks, int pendingTasks, long lastHeartbeat, double simulatedSpeed) {
        this.workerId = workerId;
        this.ip = ip;
        this.hostname = hostname;
        this.nodeType = nodeType;
        this.cpuCores = cpuCores;
        this.ramMB = ramMB;
        this.cpuFrequencyGHz = cpuFrequencyGHz;
        this.cpuUtilization = cpuUtilization;
        this.status = status;
        this.nodeId = nodeId;
        this.throughput = throughput;
        this.avgTaskTime = avgTaskTime;
        this.lamportClock = lamportClock;
        this.activeTasks = activeTasks;
        this.completedTasks = completedTasks;
        this.pendingTasks = pendingTasks;
        this.lastHeartbeat = lastHeartbeat;
        this.simulatedSpeed = simulatedSpeed;
    }

    public String getWorkerId() { return workerId; }
    public void setWorkerId(String workerId) { this.workerId = workerId; }
    public String getIp() { return ip; }
    public void setIp(String ip) { this.ip = ip; }
    public String getHostname() { return hostname; }
    public void setHostname(String hostname) { this.hostname = hostname; }
    public NodeType getNodeType() { return nodeType; }
    public void setNodeType(NodeType nodeType) { this.nodeType = nodeType; }
    public int getCpuCores() { return cpuCores; }
    public void setCpuCores(int cpuCores) { this.cpuCores = cpuCores; }
    public long getRamMB() { return ramMB; }
    public void setRamMB(long ramMB) { this.ramMB = ramMB; }
    public double getCpuFrequencyGHz() { return cpuFrequencyGHz; }
    public void setCpuFrequencyGHz(double cpuFrequencyGHz) { this.cpuFrequencyGHz = cpuFrequencyGHz; }
    public double getCpuUtilization() { return cpuUtilization; }
    public void setCpuUtilization(double cpuUtilization) { this.cpuUtilization = cpuUtilization; }
    public NodeStatus getStatus() { return status; }
    public void setStatus(NodeStatus status) { this.status = status; }
    public int getNodeId() { return nodeId; }
    public void setNodeId(int nodeId) { this.nodeId = nodeId; }
    public double getThroughput() { return throughput; }
    public void setThroughput(double throughput) { this.throughput = throughput; }
    public double getAvgTaskTime() { return avgTaskTime; }
    public void setAvgTaskTime(double avgTaskTime) { this.avgTaskTime = avgTaskTime; }
    public long getLamportClock() { return lamportClock; }
    public void setLamportClock(long lamportClock) { this.lamportClock = lamportClock; }
    public int getActiveTasks() { return activeTasks; }
    public void setActiveTasks(int activeTasks) { this.activeTasks = activeTasks; }
    public int getCompletedTasks() { return completedTasks; }
    public void setCompletedTasks(int completedTasks) { this.completedTasks = completedTasks; }
    public int getPendingTasks() { return pendingTasks; }
    public void setPendingTasks(int pendingTasks) { this.pendingTasks = pendingTasks; }
    public long getLastHeartbeat() { return lastHeartbeat; }
    public void setLastHeartbeat(long lastHeartbeat) { this.lastHeartbeat = lastHeartbeat; }
    public double getSimulatedSpeed() { return simulatedSpeed; }
    public void setSimulatedSpeed(double simulatedSpeed) { this.simulatedSpeed = simulatedSpeed; }

    // Frontend compatibility getters
    public String getId() { return workerId; }
    public String getType() { return nodeType != null ? nodeType.name() : "NODE"; }
    public double getCpuUsage() { return cpuUtilization; }

    public java.util.Map<String, Object> getSpecs() {
        java.util.Map<String, Object> specs = new java.util.HashMap<>();
        specs.put("cores", cpuCores);
        specs.put("ram", Math.max(1, ramMB / 1024));
        specs.put("speed", cpuFrequencyGHz > 0 ? cpuFrequencyGHz : 2.4);
        return specs;
    }

    public java.util.Map<String, Object> getMetrics() {
        java.util.Map<String, Object> metrics = new java.util.HashMap<>();
        metrics.put("cpuUsage", cpuUtilization);
        metrics.put("completedTasks", completedTasks);
        metrics.put("pendingTasks", pendingTasks);
        metrics.put("throughput", Math.round(throughput * 100.0) / 100.0);
        metrics.put("avgTaskTime", Math.round(avgTaskTime * 10.0) / 10.0);
        return metrics;
    }
}
