package com.dclab.worker;

import com.dclab.clock.LamportClock;
import com.dclab.model.*;
import com.dclab.rmi.SchedulerService;

import java.io.File;
import java.io.FileInputStream;
import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class WorkerNode {

    private String masterIp = "127.0.0.1";
    private int rmiPort = 1099;
    private String workerId = "Edge-01";
    private NodeType nodeType = NodeType.EDGE;
    private int nodeId = 20;
    private double speedMultiplier = 1.0;
    private int cpuCores = 4;
    private long ramMB = 8192;
    private double cpuFrequencyGHz = 2.5;

    private SchedulerService schedulerService;
    private final LamportClock lamportClock = new LamportClock();
    private ExecutorService taskExecutor;
    private ScheduledExecutorService heartbeatScheduler;
    private volatile boolean running = true;

    private final AtomicInteger activeTasks = new AtomicInteger(0);
    private final AtomicInteger completedTasks = new AtomicInteger(0);
    private final AtomicLong totalExecutionTimeMs = new AtomicLong(0);
    private long startTimeMillis;

    public static void main(String[] args) {
        WorkerNode worker = new WorkerNode();
        worker.initialize(args);
        worker.start();
    }

    public void initialize(String[] args) {
        startTimeMillis = System.currentTimeMillis();

        // 1. Try to load config/config.properties if available
        loadConfigFile();

        // 2. Read Environment Variables
        String envMasterIp = System.getenv("MASTER_IP");
        if (envMasterIp != null && !envMasterIp.isBlank()) this.masterIp = envMasterIp.trim();

        String envWorkerId = System.getenv("WORKER_ID");
        if (envWorkerId != null && !envWorkerId.isBlank()) this.workerId = envWorkerId.trim();

        String envNodeType = System.getenv("NODE_TYPE");
        if (envNodeType != null && !envNodeType.isBlank()) {
            try { this.nodeType = NodeType.valueOf(envNodeType.trim().toUpperCase()); } catch (Exception ignored) {}
        }

        String envNodeId = System.getenv("NODE_ID");
        if (envNodeId != null && !envNodeId.isBlank()) {
            try { this.nodeId = Integer.parseInt(envNodeId.trim()); } catch (Exception ignored) {}
        }

        // 3. Command-line argument overrides (highest priority)
        if (args != null) {
            for (String arg : args) {
                if (arg.startsWith("--master.ip=")) {
                    this.masterIp = arg.substring("--master.ip=".length()).trim();
                } else if (arg.startsWith("--master.port=")) {
                    this.rmiPort = Integer.parseInt(arg.substring("--master.port=".length()).trim());
                } else if (arg.startsWith("--worker.id=")) {
                    this.workerId = arg.substring("--worker.id=".length()).trim();
                } else if (arg.startsWith("--worker.type=")) {
                    try {
                        this.nodeType = NodeType.valueOf(arg.substring("--worker.type=".length()).trim().toUpperCase());
                    } catch (Exception ignored) {}
                } else if (arg.startsWith("--node.id=")) {
                    this.nodeId = Integer.parseInt(arg.substring("--node.id=".length()).trim());
                } else if (arg.startsWith("--speed=")) {
                    this.speedMultiplier = Double.parseDouble(arg.substring("--speed=".length()).trim());
                }
            }
        }

        // 4. Discover physical host hardware specs
        try {
            int detectedCores = Runtime.getRuntime().availableProcessors();
            if (detectedCores > 0) this.cpuCores = detectedCores;

            long maxMem = Runtime.getRuntime().maxMemory();
            if (maxMem > 0) this.ramMB = maxMem / (1024 * 1024);

            // Attempt to get total physical memory from MXBean if possible
            try {
                com.sun.management.OperatingSystemMXBean osBean =
                        (com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
                long totalPhysicalMem = osBean.getTotalMemorySize();
                if (totalPhysicalMem > 0) {
                    this.ramMB = totalPhysicalMem / (1024 * 1024);
                }
            } catch (Throwable ignored) {}
        } catch (Exception ignored) {}

        // Set baseline speed multiplier if not explicitly passed
        if (this.speedMultiplier <= 1.0) {
            if (this.nodeType == NodeType.CLOUD) {
                this.speedMultiplier = 5.0;
            } else {
                this.speedMultiplier = 1.0;
            }
        }

        System.out.println("==================================================");
        System.out.println("   DC-LAB HETEROGENEOUS WORKER NODE STARTING      ");
        System.out.println("==================================================");
        System.out.println("Worker ID       : " + workerId);
        System.out.println("Node ID         : " + nodeId);
        System.out.println("Node Type       : " + nodeType);
        System.out.println("Target Master   : " + masterIp + ":" + rmiPort);
        System.out.println("Detected Cores  : " + cpuCores);
        System.out.println("RAM Allocation  : " + ramMB + " MB");
        System.out.println("Speed Multiplier: " + speedMultiplier + "x");
        System.out.println("==================================================");
    }

    private void loadConfigFile() {
        File[] candidateLocations = new File[] {
                new File("config/config.properties"),
                new File("../config/config.properties"),
                new File("../../config/config.properties")
        };
        for (File f : candidateLocations) {
            if (f.exists() && f.isFile()) {
                try (FileInputStream fis = new FileInputStream(f)) {
                    Properties p = new Properties();
                    p.load(fis);
                    if (p.getProperty("master.ip") != null) this.masterIp = p.getProperty("master.ip").trim();
                    if (p.getProperty("master.rmi.port") != null) this.rmiPort = Integer.parseInt(p.getProperty("master.rmi.port").trim());
                    System.out.println("Loaded configuration defaults from: " + f.getAbsolutePath());
                    return;
                } catch (Exception ignored) {}
            }
        }
    }

    public void start() {
        // Connect to Master RMI Registry
        connectToMaster();

        // Register worker with Master
        registerWithMaster();

        // Initialize multi-core thread pool & heartbeat scheduler
        this.taskExecutor = Executors.newFixedThreadPool(Math.max(2, cpuCores));
        this.heartbeatScheduler = Executors.newSingleThreadScheduledExecutor();

        // Start Heartbeat reporting loop (every 3000 ms)
        heartbeatScheduler.scheduleAtFixedRate(this::sendHeartbeat, 1000, 3000, TimeUnit.MILLISECONDS);

        // Add shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(this::stop));

        // Start task polling execution loop
        System.out.println("[" + workerId + "] Worker operational. Polling Master for assigned tasks...");
        taskPollingLoop();
    }

    private void connectToMaster() {
        int maxRetries = 10;
        int attempt = 0;
        while (running && attempt < maxRetries) {
            try {
                attempt++;
                System.out.println("[" + workerId + "] Connecting to RMI Registry at " + masterIp + ":" + rmiPort + " (Attempt " + attempt + "/" + maxRetries + ")...");
                Registry registry = LocateRegistry.getRegistry(masterIp, rmiPort);
                this.schedulerService = (SchedulerService) registry.lookup("SchedulerService");
                System.out.println("[" + workerId + "] Successfully connected to SchedulerService at " + masterIp + ":" + rmiPort);
                return;
            } catch (Exception e) {
                System.err.println("[" + workerId + "] Connection to Master failed: " + e.getMessage());
                if (attempt < maxRetries) {
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                }
            }
        }
        System.err.println("[" + workerId + "] FATAL: Could not connect to Master RMI server at " + masterIp + ":" + rmiPort + ". Exiting.");
        System.exit(1);
    }

    private void registerWithMaster() {
        try {
            String localIp = InetAddress.getLocalHost().getHostAddress();
            String hostName = InetAddress.getLocalHost().getHostName();

            WorkerInfo info = new WorkerInfo(
                    this.workerId,
                    localIp,
                    hostName,
                    this.nodeType,
                    this.cpuCores,
                    this.ramMB,
                    this.cpuFrequencyGHz,
                    0.0,
                    NodeStatus.ONLINE,
                    this.nodeId,
                    0.0,
                    0.0,
                    lamportClock.getTime(),
                    0,
                    0,
                    0,
                    System.currentTimeMillis(),
                    this.speedMultiplier
            );

            boolean success = schedulerService.registerWorker(info);
            if (success) {
                System.out.println("[" + workerId + "] Registration approved by Master Scheduler.");
            } else {
                System.err.println("[" + workerId + "] Master rejected worker registration.");
            }
        } catch (Exception e) {
            System.err.println("[" + workerId + "] Failed to register with Master: " + e.getMessage());
        }
    }

    private void taskPollingLoop() {
        while (running) {
            try {
                Task task = null;
                try {
                    task = schedulerService.getAssignedTask(this.workerId);
                } catch (Exception e) {
                    System.err.println("[" + workerId + "] RMI call failed when getting task: " + e.getMessage());
                    // Reconnect if needed
                    Thread.sleep(2000);
                    continue;
                }

                if (task != null) {
                    final Task currentTask = task;
                    activeTasks.incrementAndGet();
                    taskExecutor.submit(() -> processTask(currentTask));
                } else {
                    // Backoff polling interval to prevent busy-waiting
                    Thread.sleep(400);
                }
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ignored) {}
            }
        }
    }

    private void processTask(Task task) {
        long taskStart = System.currentTimeMillis();
        // Update Lamport clock upon receiving task
        lamportClock.update(task.getLamportTimestamp());

        System.out.println("[" + workerId + "] Running Task [" + task.getTaskId() + "] (Complexity: " + task.getComplexity() + ")");

        // Synthetic computation workload matching complexity and hardware speed
        int computationCycles = task.getComputationSize();
        if (computationCycles <= 0) computationCycles = 50000;

        // Dynamic execution delay adjusted by worker speed multiplier
        long baseDelayMs = Math.max(50, (long) ((computationCycles / 1000.0) / speedMultiplier));
        try {
            Thread.sleep(baseDelayMs);
        } catch (InterruptedException ignored) {}

        // Verifiable CPU arithmetic calculation
        long dummySum = 0;
        int iterations = Math.min(computationCycles, 200000);
        for (int i = 0; i < iterations; i++) {
            dummySum += (long) Math.sqrt(i) * 31;
        }

        long execTime = Math.max(1, System.currentTimeMillis() - taskStart);
        totalExecutionTimeMs.addAndGet(execTime);
        completedTasks.incrementAndGet();
        activeTasks.decrementAndGet();

        // Increment clock upon sending task completion event
        long completionTimestamp = lamportClock.increment();

        TaskResult result = new TaskResult(
                task.getTaskId(),
                this.workerId,
                true,
                execTime,
                completionTimestamp,
                null
        );

        try {
            schedulerService.reportTaskCompletion(result);
            System.out.println("[" + workerId + "] Completed Task [" + task.getTaskId() + "] in " + execTime + " ms (LC: " + completionTimestamp + ")");
        } catch (Exception e) {
            System.err.println("[" + workerId + "] Error reporting task completion for " + task.getTaskId() + ": " + e.getMessage());
        }
    }

    private void sendHeartbeat() {
        if (!running) return;
        try {
            int completed = completedTasks.get();
            int active = activeTasks.get();
            long uptimeSec = Math.max(1, (System.currentTimeMillis() - startTimeMillis) / 1000);
            double throughput = (double) completed / uptimeSec;

            double cpuLoad = 0.0;
            try {
                com.sun.management.OperatingSystemMXBean osBean =
                        (com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
                double load = osBean.getCpuLoad();
                if (load >= 0) {
                    cpuLoad = Math.round(load * 100.0 * 10.0) / 10.0;
                } else {
                    cpuLoad = Math.min(100.0, active * 25.0);
                }
            } catch (Throwable ignored) {
                cpuLoad = Math.min(100.0, active * 25.0);
            }

            schedulerService.reportHeartbeat(this.workerId, cpuLoad, active, completed, throughput);
        } catch (Exception e) {
            // Heartbeat failure might indicate network blip or master restart
        }
    }

    public void stop() {
        System.out.println("[" + workerId + "] Shutting down worker node...");
        running = false;
        if (heartbeatScheduler != null) heartbeatScheduler.shutdownNow();
        if (taskExecutor != null) taskExecutor.shutdown();
    }
}
