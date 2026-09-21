package com.dclab.service;

import com.dclab.clock.EventLogger;
import com.dclab.model.LoadBalancingStatus;
import com.dclab.model.Task;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.HashMap;

@Service
public class LoadBalancerService {
    private final WorkerRegistryService workerRegistry;
    private final TaskSchedulerService taskScheduler;
    private final EventLogger eventLogger;
    private double stragglerThreshold = 0.6;
    private final ScheduledExecutorService monitorExecutor = Executors.newSingleThreadScheduledExecutor();
    private final List<String> redistributionLog = new ArrayList<>();
    private int totalRedistributions = 0;

    public LoadBalancerService(WorkerRegistryService workerRegistry, @Lazy TaskSchedulerService taskScheduler, EventLogger eventLogger) {
        this.workerRegistry = workerRegistry;
        this.taskScheduler = taskScheduler;
        this.eventLogger = eventLogger;
    }

    public Map<String, List<Task>> distributeTasksInitially(List<Task> tasks) {
        return new HashMap<>();
    }

    public void startMonitoring(String jobId) {
        monitorExecutor.scheduleAtFixedRate(this::checkAndRebalance, 2000, 2000, TimeUnit.MILLISECONDS);
    }

    public void stopMonitoring() {
    }

    public void checkAndRebalance() {
    }

    public LoadBalancingStatus getStatus() {
        return new LoadBalancingStatus();
    }

    public void rebalance() {
        totalRedistributions++;
        redistributionLog.add("Manual rebalance triggered");
    }
}
