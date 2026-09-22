package com.dclab.service;

import com.dclab.clock.EventLogger;
import com.dclab.clock.LamportClock;
import com.dclab.model.*;
import com.dclab.replication.ReplicationManager;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Service
public class TaskSchedulerService {
    private final ConcurrentHashMap<String, Job> jobs = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Task> tasks = new ConcurrentHashMap<>();
    private final ConcurrentLinkedQueue<Task> pendingQueue = new ConcurrentLinkedQueue<>();
    private final ExecutorService schedulerExecutor = Executors.newCachedThreadPool();
    private final LamportClock lamportClock;
    private final EventLogger eventLogger;
    private final WorkerRegistryService workerRegistry;
    private final LoadBalancerService loadBalancer;
    private final ReplicationManager replicationManager;

    public TaskSchedulerService(LamportClock lamportClock, EventLogger eventLogger, 
                                WorkerRegistryService workerRegistry, @Lazy LoadBalancerService loadBalancer, 
                                ReplicationManager replicationManager) {
        this.lamportClock = lamportClock;
        this.eventLogger = eventLogger;
        this.workerRegistry = workerRegistry;
        this.loadBalancer = loadBalancer;
        this.replicationManager = replicationManager;
    }

    public Job createJob(int numTasks, TaskComplexity complexity) {
        String jobId = UUID.randomUUID().toString();
        List<String> taskIds = new ArrayList<>();
        for (int i = 0; i < numTasks; i++) {
            Task task = new Task(UUID.randomUUID().toString(), jobId, null, TaskStatus.PENDING, 0, 0, 0, lamportClock.getTime(), complexity, complexity.getComputationSize(), 0.0);
            tasks.put(task.getTaskId(), task);
            taskIds.add(task.getTaskId());
            pendingQueue.add(task);
        }
        Job job = new Job(jobId, numTasks, 0, numTasks, 0, 0, System.currentTimeMillis(), 0, "CREATED", complexity, taskIds);
        jobs.put(jobId, job);
        return job;
    }

    public void startJob(String jobId) {
        Job job = jobs.get(jobId);
        if (job != null) {
            job.setStatus("RUNNING");
            job.setStartTime(System.currentTimeMillis());
            List<Task> jobTasks = getTasksByJob(jobId);
            loadBalancer.distributeTasksInitially(jobTasks);
            loadBalancer.startMonitoring(jobId);
        }
    }

    public Task getNextTask(String workerId) {
        // First check for tasks pre-assigned to this worker
        for (Task t : pendingQueue) {
            if (t.getAssignedWorker() != null && t.getAssignedWorker().equals(workerId) && t.getStatus() == TaskStatus.ASSIGNED) {
                pendingQueue.remove(t);
                t.setStatus(TaskStatus.RUNNING);
                t.setStartTime(System.currentTimeMillis());
                return t;
            }
        }
        // Fallback: assign an unassigned pending task if available
        for (Task t : pendingQueue) {
            if (t.getAssignedWorker() == null && t.getStatus() == TaskStatus.PENDING) {
                pendingQueue.remove(t);
                t.setAssignedWorker(workerId);
                t.setStatus(TaskStatus.RUNNING);
                t.setStartTime(System.currentTimeMillis());
                return t;
            }
        }
        return null;
    }

    public void completeTask(TaskResult result) {
        Task task = tasks.get(result.getTaskId());
        if (task != null) {
            task.setStatus(TaskStatus.COMPLETED);
            task.setEndTime(System.currentTimeMillis());
            task.setExecutionTimeMs(result.getExecutionTimeMs());
            Job job = jobs.get(task.getJobId());
            if (job != null) {
                job.setCompletedTasks(job.getCompletedTasks() + 1);
                job.setRunningTasks(Math.max(0, job.getRunningTasks() - 1));
                job.setPendingTasks(Math.max(0, job.getPendingTasks() - 1));
                if (job.getCompletedTasks() >= job.getTotalTasks()) {
                    job.setStatus("COMPLETED");
                    job.setEndTime(System.currentTimeMillis());
                }
            }
            lamportClock.update(result.getLamportTimestamp());
            eventLogger.logEvent(result.getWorkerId(), "TASK_COMPLETED", lamportClock.getTime(), task.getTaskId(), "Completed task in " + result.getExecutionTimeMs() + "ms");
            replicationManager.updatePrimaryState(new SchedulerState());
        }
    }

    public void failTask(String taskId, String reason) {
        Task task = tasks.get(taskId);
        if (task != null) {
            task.setStatus(TaskStatus.FAILED);
        }
    }

    public void reassignTask(String taskId, String newWorkerId) {
        Task task = tasks.get(taskId);
        if (task != null) {
            task.setAssignedWorker(newWorkerId);
            task.setStatus(TaskStatus.ASSIGNED);
            if (!pendingQueue.contains(task)) {
                pendingQueue.add(task);
            }
        }
    }

    public Job getJob(String jobId) { return jobs.get(jobId); }
    public List<Job> getAllJobs() { return new ArrayList<>(jobs.values()); }
    public List<Task> getAllTasks() { return new ArrayList<>(tasks.values()); }
    public List<Task> getTasksByJob(String jobId) {
        return tasks.values().stream().filter(t -> t.getJobId().equals(jobId)).collect(Collectors.toList());
    }
    public List<Task> getTasksByWorker(String workerId) {
        return tasks.values().stream().filter(t -> workerId.equals(t.getAssignedWorker())).collect(Collectors.toList());
    }
    public Map<String, List<Task>> getTaskDistribution() {
        return tasks.values().stream().filter(t -> t.getAssignedWorker() != null)
                .collect(Collectors.groupingBy(Task::getAssignedWorker));
    }
}
