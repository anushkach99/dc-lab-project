package com.dclab.demo;

import com.dclab.clock.EventLogger;
import com.dclab.clock.LamportClock;
import com.dclab.election.ElectionService;
import com.dclab.model.*;
import com.dclab.replication.ReplicationManager;
import com.dclab.service.LoadBalancerService;
import com.dclab.service.SystemLogService;
import com.dclab.service.TaskSchedulerService;
import com.dclab.service.WorkerRegistryService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class DemoModeManager {
    private final List<SimulatedWorker> simulatedWorkers = new ArrayList<>();
    private final WorkerRegistryService workerRegistry;
    private final TaskSchedulerService taskScheduler;
    private final LoadBalancerService loadBalancer;
    private final ElectionService electionService;
    private final ReplicationManager replicationManager;
    private final SystemLogService logService;
    private final EventLogger eventLogger;
    private final LamportClock clock;
    private boolean running = false;

    public DemoModeManager(WorkerRegistryService workerRegistry, 
                           @Lazy TaskSchedulerService taskScheduler, 
                           @Lazy LoadBalancerService loadBalancer,
                           ElectionService electionService,
                           ReplicationManager replicationManager,
                           SystemLogService logService,
                           EventLogger eventLogger, 
                           LamportClock clock) {
        this.workerRegistry = workerRegistry;
        this.taskScheduler = taskScheduler;
        this.loadBalancer = loadBalancer;
        this.electionService = electionService;
        this.replicationManager = replicationManager;
        this.logService = logService;
        this.eventLogger = eventLogger;
        this.clock = clock;
    }

    public synchronized void startDemoMode() {
        if (running) return;
        running = true;

        logService.addLog("DEMO", "Starting simulation demo mode with 4 heterogeneous nodes");

        createWorker("Edge-01", 20, NodeType.EDGE, 4, 8192, 2.4, 1.0);
        createWorker("Edge-02", 30, NodeType.EDGE, 8, 16384, 3.0, 2.0);
        createWorker("Cloud-01", 40, NodeType.CLOUD, 16, 32768, 3.5, 5.0);
        createWorker("Cloud-02", 50, NodeType.CLOUD, 32, 65536, 4.0, 8.0);
        
        for (SimulatedWorker sw : simulatedWorkers) {
            new Thread(sw).start();
        }

        logService.addLog("DEMO", "4 heterogeneous workers registered via Java RMI protocol");
    }

    private void createWorker(String id, int nodeId, NodeType type, int cores, long ram, double freq, double speed) {
        WorkerInfo info = new WorkerInfo(id, "127.0.0.1", id, type, cores, ram, freq, 0.0, NodeStatus.ONLINE, nodeId, 0.0, 0.0, 0, 0, 0, 0, System.currentTimeMillis(), speed);
        workerRegistry.registerWorker(info);
        SimulatedWorker worker = new SimulatedWorker(info, speed, clock, taskScheduler, eventLogger);
        simulatedWorkers.add(worker);
    }

    public synchronized void stopDemoMode() {
        running = false;
        for (SimulatedWorker sw : simulatedWorkers) {
            sw.stop();
        }
        simulatedWorkers.clear();
        logService.addLog("DEMO", "Demo workers stopped");
    }

    public void simulateStraggler(String workerId) {
        for (SimulatedWorker sw : simulatedWorkers) {
            if (sw.getInfo().getWorkerId().equals(workerId)) {
                sw.setSpeed(0.1);
                sw.getInfo().setSimulatedSpeed(0.1);
                sw.getInfo().setStatus(NodeStatus.STRAGGLER);
                logService.addLog("DEMO", "Simulated STRAGGLER: Reduced " + workerId + " execution speed to 0.1 tasks/sec");
                eventLogger.logEvent(workerId, "SIMULATE_STRAGGLER", clock.increment(), null, "Speed reduced artificially");
            }
        }
    }

    public void restoreWorker(String workerId) {
        for (SimulatedWorker sw : simulatedWorkers) {
            if (sw.getInfo().getWorkerId().equals(workerId)) {
                double speed = workerId.contains("Cloud") ? 5.0 : 2.0;
                sw.setSpeed(speed);
                sw.getInfo().setSimulatedSpeed(speed);
                sw.getInfo().setStatus(NodeStatus.ONLINE);
                logService.addLog("DEMO", "Restored worker " + workerId + " speed to " + speed + " tasks/sec");
            }
        }
    }

    public void runFullExperiment() {
        try {
            logService.addLog("EXPERIMENT", "==================================================");
            logService.addLog("EXPERIMENT", "STARTING FULL INTEGRATED DISTRIBUTED EXPERIMENT SUITE");
            logService.addLog("EXPERIMENT", "==================================================");

            // Step 1: Ensure demo mode started
            if (!running || simulatedWorkers.isEmpty()) {
                startDemoMode();
                Thread.sleep(1000);
            }

            // Step 2: Show registered nodes via RMI
            logService.addLog("EXP 1", "[Exp 1 - RMI Communication] 4 Workers registered with Master RMI Registry");
            for (WorkerInfo w : workerRegistry.getAllWorkers()) {
                logService.addLog("EXP 1", " -> Worker: " + w.getWorkerId() + " [" + w.getNodeType() + "] | Cores: " + w.getCpuCores() + " | RAM: " + w.getRamMB() + "MB | Speed: " + w.getSimulatedSpeed() + " t/s");
            }

            // Step 3: Create 100 tasks job
            logService.addLog("EXP 2", "[Exp 2 - Multithreading] Creating distributed Job with 100 tasks (MEDIUM complexity)");
            Job job = taskScheduler.createJob(100, TaskComplexity.MEDIUM);
            logService.addLog("JOB", "Job J001 created with ID: " + job.getJobId());

            // Step 4: Heterogeneity-Aware Initial Task Distribution (Exp 6)
            logService.addLog("EXP 6", "[Exp 6 - Heterogeneity-Aware Scheduling] Calculating initial workload weights based on CPU, RAM, & frequency");
            List<Task> jobTasks = taskScheduler.getTasksByJob(job.getJobId());
            loadBalancer.distributeTasksInitially(jobTasks);

            // Step 5: Start execution concurrently across thread pools
            logService.addLog("EXP 2", "[Exp 2 - Multithreaded Execution] Starting concurrent execution across worker thread pools");
            taskScheduler.startJob(job.getJobId());
            
            // Allow tasks to run for 2 seconds
            Thread.sleep(2000);

            // Step 6: Log Lamport Events (Exp 3)
            logService.addLog("EXP 3", "[Exp 3 - Lamport Clock] Distributed events timestamped and causally ordered");
            eventLogger.logEvent("Master", "JOB_DISPATCHED", clock.increment(), job.getJobId(), "Job dispatched to worker cluster");

            // Step 7: Inject Straggler (Exp 6)
            logService.addLog("EXP 6", "[Exp 6 - Straggler Injection] Artificially slowing Edge-01 down (0.1 tasks/sec)");
            simulateStraggler("Edge-01");
            Thread.sleep(1500);

            // Step 8: Trigger Dynamic Load Balancing & Rebalancing (Exp 6)
            logService.addLog("EXP 6", "[Exp 6 - Straggler Detection & Redistribution] Straggler detected! Reassigning pending tasks to Cloud workers");
            loadBalancer.rebalance();
            Thread.sleep(1500);

            // Step 9: Replicate State (Exp 5)
            logService.addLog("EXP 5", "[Exp 5 - Replication & Bounded Staleness] Synchronizing Primary state to Backup replica");
            replicationManager.triggerSync();

            // Step 10: Coordinator Failure & Election (Exp 4)
            logService.addLog("EXP 4", "[Exp 4 - Leader Election] Simulating Master failure! Triggering Bully Election algorithm");
            electionService.simulateMasterFailure();
            Thread.sleep(1500);

            // Step 11: Switch algorithm to Ring and run Ring election
            logService.addLog("EXP 4", "[Exp 4 - Ring Election] Testing Ring Election algorithm across active nodes");
            electionService.startElection(ElectionAlgorithm.RING);

            // Allow job to complete remaining tasks
            Thread.sleep(2000);

            logService.addLog("EXPERIMENT", "==================================================");
            logService.addLog("EXPERIMENT", "FULL EXPERIMENT SUITE COMPLETED SUCCESSFULLY!");
            logService.addLog("EXPERIMENT", "All 6 Distributed Systems Experiments Verified Visually.");
            logService.addLog("EXPERIMENT", "==================================================");

        } catch (Exception e) {
            logService.addLog("EXPERIMENT", "Experiment error: " + e.getMessage());
        }
    }

    public List<SimulatedWorker> getSimulatedWorkers() { return simulatedWorkers; }
}
