package com.dclab.model;

import java.util.List;
import java.util.Map;

public class LoadBalancingStatus {
    private Map<String, Integer> taskDistribution;
    private Map<String, Double> workerThroughput;
    private Map<String, Integer> completedPerWorker;
    private Map<String, Integer> remainingPerWorker;
    private List<String> stragglerIds;
    private List<String> idleWorkerIds;
    private int totalRedistributions;
    private List<String> redistributionLog;

    public LoadBalancingStatus() {}

    public Map<String, Integer> getTaskDistribution() { return taskDistribution; }
    public void setTaskDistribution(Map<String, Integer> taskDistribution) { this.taskDistribution = taskDistribution; }
    public Map<String, Double> getWorkerThroughput() { return workerThroughput; }
    public void setWorkerThroughput(Map<String, Double> workerThroughput) { this.workerThroughput = workerThroughput; }
    public Map<String, Integer> getCompletedPerWorker() { return completedPerWorker; }
    public void setCompletedPerWorker(Map<String, Integer> completedPerWorker) { this.completedPerWorker = completedPerWorker; }
    public Map<String, Integer> getRemainingPerWorker() { return remainingPerWorker; }
    public void setRemainingPerWorker(Map<String, Integer> remainingPerWorker) { this.remainingPerWorker = remainingPerWorker; }
    public List<String> getStragglerIds() { return stragglerIds; }
    public void setStragglerIds(List<String> stragglerIds) { this.stragglerIds = stragglerIds; }
    public List<String> getIdleWorkerIds() { return idleWorkerIds; }
    public void setIdleWorkerIds(List<String> idleWorkerIds) { this.idleWorkerIds = idleWorkerIds; }
    public int getTotalRedistributions() { return totalRedistributions; }
    public void setTotalRedistributions(int totalRedistributions) { this.totalRedistributions = totalRedistributions; }
    public List<String> getRedistributionLog() { return redistributionLog; }
    public void setRedistributionLog(List<String> redistributionLog) { this.redistributionLog = redistributionLog; }
}
