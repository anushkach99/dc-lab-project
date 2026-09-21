package com.dclab.demo;

import com.dclab.clock.EventLogger;
import com.dclab.clock.LamportClock;
import com.dclab.model.NodeStatus;
import com.dclab.model.NodeType;
import com.dclab.model.WorkerInfo;
import com.dclab.service.TaskSchedulerService;
import com.dclab.service.WorkerRegistryService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class DemoModeManager {
    private final List<SimulatedWorker> simulatedWorkers = new ArrayList<>();
    private final WorkerRegistryService workerRegistry;
    private final TaskSchedulerService taskScheduler;
    private final EventLogger eventLogger;
    private boolean running = false;
    private final LamportClock clock;

    public DemoModeManager(WorkerRegistryService workerRegistry, @Lazy TaskSchedulerService taskScheduler, EventLogger eventLogger, LamportClock clock) {
        this.workerRegistry = workerRegistry;
        this.taskScheduler = taskScheduler;
        this.eventLogger = eventLogger;
        this.clock = clock;
    }

    public void startDemoMode() {
        if (running) return;
        running = true;

        createWorker("Edge-01", 20, NodeType.EDGE, 4, 8192, 2.4, 1.0);
        createWorker("Edge-02", 30, NodeType.EDGE, 8, 16384, 3.0, 2.0);
        createWorker("Cloud-01", 40, NodeType.CLOUD, 16, 32768, 3.5, 5.0);
        createWorker("Cloud-02", 50, NodeType.CLOUD, 32, 65536, 4.0, 8.0);
        
        for (SimulatedWorker sw : simulatedWorkers) {
            new Thread(sw).start();
        }
    }

    private void createWorker(String id, int nodeId, NodeType type, int cores, long ram, double freq, double speed) {
        WorkerInfo info = new WorkerInfo(id, "127.0.0.1", id, type, cores, ram, freq, 0.0, NodeStatus.ONLINE, nodeId, 0.0, 0.0, 0, 0, 0, 0, System.currentTimeMillis(), speed);
        workerRegistry.registerWorker(info);
        SimulatedWorker worker = new SimulatedWorker(info, speed, clock, taskScheduler, eventLogger);
        simulatedWorkers.add(worker);
    }

    public void stopDemoMode() {
        running = false;
        for (SimulatedWorker sw : simulatedWorkers) {
            sw.stop();
        }
        simulatedWorkers.clear();
    }

    public void simulateStraggler(String workerId) {
        for (SimulatedWorker sw : simulatedWorkers) {
            if (sw.getInfo().getWorkerId().equals(workerId)) {
                sw.setSpeed(0.1);
                sw.getInfo().setSimulatedSpeed(0.1);
            }
        }
    }

    public void restoreWorker(String workerId) {
        for (SimulatedWorker sw : simulatedWorkers) {
            if (sw.getInfo().getWorkerId().equals(workerId)) {
                // Restore logic - in real scenario would store original speed
                sw.setSpeed(5.0);
                sw.getInfo().setSimulatedSpeed(5.0);
            }
        }
    }

    public void runFullExperiment() {
        // Orchestrates the experiment
    }

    public List<SimulatedWorker> getSimulatedWorkers() {
        return simulatedWorkers;
    }
}
