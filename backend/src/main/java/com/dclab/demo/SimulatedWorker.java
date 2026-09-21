package com.dclab.demo;

import com.dclab.clock.EventLogger;
import com.dclab.clock.LamportClock;
import com.dclab.model.Task;
import com.dclab.model.TaskResult;
import com.dclab.model.WorkerInfo;
import com.dclab.service.TaskSchedulerService;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SimulatedWorker implements Runnable {
    private final WorkerInfo info;
    private double speed;
    private final ExecutorService executor;
    private volatile boolean running = true;
    private final LamportClock clock;
    private final TaskSchedulerService taskScheduler;
    private final EventLogger eventLogger;
    private final Queue<Task> assignedTasks = new ConcurrentLinkedQueue<>();

    public SimulatedWorker(WorkerInfo info, double speed, LamportClock clock, TaskSchedulerService taskScheduler, EventLogger eventLogger) {
        this.info = info;
        this.speed = speed;
        this.clock = clock;
        this.taskScheduler = taskScheduler;
        this.eventLogger = eventLogger;
        this.executor = Executors.newFixedThreadPool(info.getCpuCores());
    }

    public WorkerInfo getInfo() { return info; }
    public void setSpeed(double speed) { this.speed = speed; }
    
    public void stop() {
        this.running = false;
        executor.shutdown();
    }

    @Override
    public void run() {
        while (running) {
            try {
                Task nextTask = taskScheduler.getNextTask(info.getWorkerId());
                if (nextTask != null) {
                    executeTask(nextTask);
                } else {
                    Thread.sleep(500);
                }
                
                // Heartbeat simulation
                info.setLastHeartbeat(System.currentTimeMillis());
                
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private void executeTask(Task task) {
        long startTime = System.currentTimeMillis();
        long delayMs = (long) ((task.getComplexity().getComputationSize() / 1000) / speed);
        
        try {
            Thread.sleep(delayMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Dummy computation to use CPU
        long result = 0;
        for (int i = 0; i < task.getComputationSize(); i++) {
            result += i;
        }
        
        long execTime = System.currentTimeMillis() - startTime;
        long ts = clock.increment();
        
        TaskResult taskResult = new TaskResult(task.getTaskId(), info.getWorkerId(), true, execTime, ts, null);
        taskScheduler.completeTask(taskResult);
        eventLogger.logEvent(info.getWorkerId(), "TASK_COMPLETE", ts, task.getTaskId(), "Completed in " + execTime + "ms");
    }
}
