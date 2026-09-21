package com.dclab.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class LoadBalancingStatus implements Serializable {
    private Map<String, Integer> taskDistribution;
    private Map<String, Double> workerThroughput;
    private Map<String, Integer> completedPerWorker;
    private Map<String, Integer> remainingPerWorker;
    private List<String> stragglerIds;
    private List<String> idleWorkerIds;
    private int totalRedistributions;
    private int redistributionCount;
    private int stragglersDetected;
    private int totalTasks;
    private double threshold = 0.6;
    private List<String> redistributionLog = new ArrayList<>();
    private List<RedistributionEntry> redistributions = new ArrayList<>();

    public LoadBalancingStatus() {}

    public static class RedistributionEntry implements Serializable {
        private long timestamp;
        private String source;
        private String target;
        private int taskCount;

        public RedistributionEntry() {}

        public RedistributionEntry(long timestamp, String source, String target, int taskCount) {
            this.timestamp = timestamp;
            this.source = source;
            this.target = target;
            this.taskCount = taskCount;
        }

        public long getTimestamp() { return timestamp; }
        public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
        public String getSource() { return source; }
        public void setSource(String source) { this.source = source; }
        public String getTarget() { return target; }
        public void setTarget(String target) { this.target = target; }
        public int getTaskCount() { return taskCount; }
        public void setTaskCount(int taskCount) { this.taskCount = taskCount; }
    }

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
    public void setTotalRedistributions(int totalRedistributions) { 
        this.totalRedistributions = totalRedistributions;
        this.redistributionCount = totalRedistributions;
    }
    public int getRedistributionCount() { return redistributionCount; }
    public void setRedistributionCount(int redistributionCount) { 
        this.redistributionCount = redistributionCount;
        this.totalRedistributions = redistributionCount;
    }
    public int getStragglersDetected() { return stragglersDetected; }
    public void setStragglersDetected(int stragglersDetected) { this.stragglersDetected = stragglersDetected; }
    public int getTotalTasks() { return totalTasks; }
    public void setTotalTasks(int totalTasks) { this.totalTasks = totalTasks; }
    public double getThreshold() { return threshold; }
    public void setThreshold(double threshold) { this.threshold = threshold; }
    public List<String> getRedistributionLog() { return redistributionLog; }
    public void setRedistributionLog(List<String> redistributionLog) { this.redistributionLog = redistributionLog; }
    public List<RedistributionEntry> getRedistributions() { return redistributions; }
    public void setRedistributions(List<RedistributionEntry> redistributions) { this.redistributions = redistributions; }
}
