package com.dclab.service;

import com.dclab.clock.EventLogger;
import com.dclab.model.*;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class LoadBalancerService {
    private final WorkerRegistryService workerRegistry;
    private final TaskSchedulerService taskScheduler;
    private final EventLogger eventLogger;
    private final SystemLogService logService;
    private double stragglerThreshold = 0.6;
    private final ScheduledExecutorService monitorExecutor = Executors.newSingleThreadScheduledExecutor();
    private final List<String> redistributionLog = new CopyOnWriteArrayList<>();
    private final List<LoadBalancingStatus.RedistributionEntry> redistributionEntries = new CopyOnWriteArrayList<>();
    private int totalRedistributions = 0;
    private int stragglersDetectedCount = 0;
    private String currentJobId;

    public LoadBalancerService(WorkerRegistryService workerRegistry, 
                               @Lazy TaskSchedulerService taskScheduler, 
                               EventLogger eventLogger,
                               SystemLogService logService) {
        this.workerRegistry = workerRegistry;
        this.taskScheduler = taskScheduler;
        this.eventLogger = eventLogger;
        this.logService = logService;
    }

    public Map<String, List<Task>> distributeTasksInitially(List<Task> tasks) {
        List<WorkerInfo> onlineWorkers = workerRegistry.getOnlineWorkers();
        Map<String, List<Task>> distribution = new HashMap<>();

        if (onlineWorkers.isEmpty() || tasks == null || tasks.isEmpty()) {
            return distribution;
        }

        // Calculate capability weight for each worker
        double totalCapacity = 0.0;
        Map<String, Double> weights = new HashMap<>();
        for (WorkerInfo w : onlineWorkers) {
            double weight = w.getCpuCores() * (w.getCpuFrequencyGHz() > 0 ? w.getCpuFrequencyGHz() : 2.0) 
                    * (1.0 - (w.getCpuUtilization() / 100.0)) * w.getSimulatedSpeed();
            if (weight <= 0) weight = 0.5;
            weights.put(w.getWorkerId(), weight);
            totalCapacity += weight;
            distribution.put(w.getWorkerId(), new ArrayList<>());
        }

        // Distribute tasks proportionally
        int totalTasks = tasks.size();
        int assignedCount = 0;

        for (int i = 0; i < onlineWorkers.size(); i++) {
            WorkerInfo w = onlineWorkers.get(i);
            int count;
            if (i == onlineWorkers.size() - 1) {
                count = totalTasks - assignedCount;
            } else {
                count = (int) Math.round((weights.get(w.getWorkerId()) / totalCapacity) * totalTasks);
                if (assignedCount + count > totalTasks) count = totalTasks - assignedCount;
            }

            for (int k = 0; k < count; k++) {
                if (assignedCount < totalTasks) {
                    Task task = tasks.get(assignedCount);
                    task.setAssignedWorker(w.getWorkerId());
                    task.setStatus(TaskStatus.ASSIGNED);
                    distribution.get(w.getWorkerId()).add(task);
                    w.setPendingTasks(w.getPendingTasks() + 1);
                    assignedCount++;
                }
            }
        }

        logService.addLog("LB", "Initial heterogeneous task distribution: " + 
            distribution.entrySet().stream()
                .map(e -> e.getKey() + "=" + e.getValue().size())
                .collect(Collectors.joining(", ")));

        return distribution;
    }

    public void startMonitoring(String jobId) {
        this.currentJobId = jobId;
        monitorExecutor.scheduleAtFixedRate(this::checkAndRebalance, 1000, 1500, TimeUnit.MILLISECONDS);
    }

    public void checkAndRebalance() {
        try {
            List<WorkerInfo> onlineWorkers = workerRegistry.getOnlineWorkers();
            if (onlineWorkers.isEmpty()) return;

            // Compute worker statistics & average throughput
            double totalThroughput = 0;
            int activeWorkersCount = 0;
            List<String> stragglers = new ArrayList<>();
            List<String> idleWorkers = new ArrayList<>();

            for (WorkerInfo w : onlineWorkers) {
                if (w.getPendingTasks() > 0 || w.getCompletedTasks() > 0) {
                    activeWorkersCount++;
                    totalThroughput += w.getThroughput();
                }
            }

            double avgThroughput = activeWorkersCount > 0 ? totalThroughput / activeWorkersCount : 0;

            // Identify stragglers and idle workers
            for (WorkerInfo w : onlineWorkers) {
                // If a worker has low throughput compared to average AND pending tasks, or is explicitly simulated slow
                boolean isSlow = (w.getSimulatedSpeed() < 0.5) || 
                                (avgThroughput > 0 && w.getPendingTasks() > 5 && w.getThroughput() < stragglerThreshold * avgThroughput);
                
                if (isSlow && w.getPendingTasks() > 0) {
                    if (w.getStatus() != NodeStatus.STRAGGLER) {
                        w.setStatus(NodeStatus.STRAGGLER);
                        stragglers.add(w.getWorkerId());
                        stragglersDetectedCount++;
                        logService.addLog("LB", "Worker " + w.getWorkerId() + " identified as STRAGGLER (Speed: " + w.getSimulatedSpeed() + ")");
                        eventLogger.logEvent(w.getWorkerId(), "STRAGGLER_DETECTED", 0, null, "Worker throughput below threshold");
                    }
                } else if (w.getPendingTasks() == 0 && w.getCompletedTasks() > 0) {
                    if (w.getStatus() != NodeStatus.IDLE && w.getStatus() != NodeStatus.STRAGGLER) {
                        w.setStatus(NodeStatus.IDLE);
                        idleWorkers.add(w.getWorkerId());
                    }
                }
            }

            // Perform task redistribution if stragglers detected or underloaded workers available
            for (WorkerInfo straggler : onlineWorkers) {
                if (straggler.getStatus() == NodeStatus.STRAGGLER && straggler.getPendingTasks() > 3) {
                    // Find fastest online worker that is not a straggler
                    WorkerInfo target = onlineWorkers.stream()
                            .filter(w -> w.getStatus() != NodeStatus.STRAGGLER && w.getStatus() != NodeStatus.OFFLINE)
                            .max(Comparator.comparingDouble(WorkerInfo::getSimulatedSpeed))
                            .orElse(null);

                    if (target != null && !target.getWorkerId().equals(straggler.getWorkerId())) {
                        int tasksToMove = straggler.getPendingTasks() / 2;
                        if (tasksToMove > 0) {
                            List<Task> stragglerTasks = taskScheduler.getTasksByWorker(straggler.getWorkerId());
                            int moved = 0;
                            for (Task t : stragglerTasks) {
                                if (t.getStatus() == TaskStatus.ASSIGNED && moved < tasksToMove) {
                                    taskScheduler.reassignTask(t.getTaskId(), target.getWorkerId());
                                    straggler.setPendingTasks(Math.max(0, straggler.getPendingTasks() - 1));
                                    target.setPendingTasks(target.getPendingTasks() + 1);
                                    moved++;
                                }
                            }

                            if (moved > 0) {
                                totalRedistributions++;
                                String logMsg = moved + " pending tasks reassigned from " + straggler.getWorkerId() + " to " + target.getWorkerId();
                                redistributionLog.add(logMsg);
                                redistributionEntries.add(new LoadBalancingStatus.RedistributionEntry(
                                        System.currentTimeMillis(), straggler.getWorkerId(), target.getWorkerId(), moved));
                                logService.addLog("LB", logMsg);
                                eventLogger.logEvent(target.getWorkerId(), "TASK_REDISTRIBUTED", 0, null, logMsg);
                            }
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public LoadBalancingStatus getStatus() {
        LoadBalancingStatus status = new LoadBalancingStatus();
        List<WorkerInfo> workers = workerRegistry.getAllWorkers();

        Map<String, Integer> dist = new HashMap<>();
        Map<String, Double> tp = new HashMap<>();
        Map<String, Integer> completed = new HashMap<>();
        Map<String, Integer> remaining = new HashMap<>();
        List<String> stragglers = new ArrayList<>();
        List<String> idle = new ArrayList<>();
        int totalTasks = 0;

        for (WorkerInfo w : workers) {
            dist.put(w.getWorkerId(), w.getCompletedTasks() + w.getPendingTasks());
            tp.put(w.getWorkerId(), w.getThroughput());
            completed.put(w.getWorkerId(), w.getCompletedTasks());
            remaining.put(w.getWorkerId(), w.getPendingTasks());
            totalTasks += (w.getCompletedTasks() + w.getPendingTasks());
            if (w.getStatus() == NodeStatus.STRAGGLER) stragglers.add(w.getWorkerId());
            if (w.getStatus() == NodeStatus.IDLE) idle.add(w.getWorkerId());
        }

        status.setTaskDistribution(dist);
        status.setWorkerThroughput(tp);
        status.setCompletedPerWorker(completed);
        status.setRemainingPerWorker(remaining);
        status.setStragglerIds(stragglers);
        status.setIdleWorkerIds(idle);
        status.setTotalRedistributions(totalRedistributions);
        status.setRedistributionCount(totalRedistributions);
        status.setStragglersDetected(stragglersDetectedCount);
        status.setTotalTasks(totalTasks);
        status.setThreshold(stragglerThreshold);
        status.setRedistributionLog(new ArrayList<>(redistributionLog));
        status.setRedistributions(new ArrayList<>(redistributionEntries));

        return status;
    }

    public void rebalance() {
        checkAndRebalance();
        totalRedistributions++;
        redistributionLog.add("Manual rebalance executed");
    }

    public double getStragglerThreshold() { return stragglerThreshold; }
    public void setStragglerThreshold(double stragglerThreshold) { this.stragglerThreshold = stragglerThreshold; }
}
