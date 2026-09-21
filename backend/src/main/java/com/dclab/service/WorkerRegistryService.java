package com.dclab.service;

import com.dclab.model.NodeStatus;
import com.dclab.model.WorkerInfo;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class WorkerRegistryService {
    private final ConcurrentHashMap<String, WorkerInfo> workers = new ConcurrentHashMap<>();
    private final ScheduledExecutorService heartbeatChecker = Executors.newSingleThreadScheduledExecutor();

    public WorkerRegistryService() {
        heartbeatChecker.scheduleAtFixedRate(this::checkHeartbeats, 6000, 6000, TimeUnit.MILLISECONDS);
    }

    public void registerWorker(WorkerInfo worker) {
        workers.put(worker.getWorkerId(), worker);
    }

    public void removeWorker(String workerId) {
        workers.remove(workerId);
    }

    public WorkerInfo getWorker(String workerId) {
        return workers.get(workerId);
    }

    public List<WorkerInfo> getAllWorkers() {
        return new ArrayList<>(workers.values());
    }

    public List<WorkerInfo> getOnlineWorkers() {
        return workers.values().stream()
                .filter(w -> w.getStatus() != NodeStatus.OFFLINE)
                .collect(Collectors.toList());
    }

    public void updateHeartbeat(String workerId, double cpuUtil, int activeTasks, int completed, double throughput) {
        WorkerInfo w = workers.get(workerId);
        if (w != null) {
            w.setCpuUtilization(cpuUtil);
            w.setActiveTasks(activeTasks);
            w.setCompletedTasks(completed);
            w.setThroughput(throughput);
            w.setLastHeartbeat(System.currentTimeMillis());
            if (w.getStatus() == NodeStatus.OFFLINE) {
                w.setStatus(NodeStatus.ONLINE);
            }
        }
    }

    public void checkHeartbeats() {
        long now = System.currentTimeMillis();
        for (WorkerInfo w : workers.values()) {
            if (now - w.getLastHeartbeat() > 6000) {
                w.setStatus(NodeStatus.OFFLINE);
            }
        }
    }

    public int getOnlineWorkerCount() {
        return getOnlineWorkers().size();
    }
}
