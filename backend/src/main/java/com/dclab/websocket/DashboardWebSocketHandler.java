package com.dclab.websocket;

import com.dclab.clock.EventLogger;
import com.dclab.config.AppConfig;
import com.dclab.election.ElectionService;
import com.dclab.model.Job;
import com.dclab.model.LamportEvent;
import com.dclab.model.SystemStatus;
import com.dclab.model.Task;
import com.dclab.model.TaskStatus;
import com.dclab.replication.ReplicationManager;
import com.dclab.service.LoadBalancerService;
import com.dclab.service.SystemLogService;
import com.dclab.service.TaskSchedulerService;
import com.dclab.service.WorkerRegistryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@EnableScheduling
public class DashboardWebSocketHandler {

    private final SimpMessagingTemplate messagingTemplate;
    private final WorkerRegistryService workerRegistry;
    private final TaskSchedulerService taskScheduler;
    private final LoadBalancerService loadBalancer;
    private final ElectionService electionService;
    private final ReplicationManager replicationManager;
    private final EventLogger eventLogger;
    private final SystemLogService logService;
    private final AppConfig appConfig;

    @Autowired
    public DashboardWebSocketHandler(SimpMessagingTemplate messagingTemplate,
                                     WorkerRegistryService workerRegistry,
                                     TaskSchedulerService taskScheduler,
                                     LoadBalancerService loadBalancer,
                                     ElectionService electionService,
                                     ReplicationManager replicationManager,
                                     EventLogger eventLogger,
                                     SystemLogService logService,
                                     AppConfig appConfig) {
        this.messagingTemplate = messagingTemplate;
        this.workerRegistry = workerRegistry;
        this.taskScheduler = taskScheduler;
        this.loadBalancer = loadBalancer;
        this.electionService = electionService;
        this.replicationManager = replicationManager;
        this.eventLogger = eventLogger;
        this.logService = logService;
        this.appConfig = appConfig;
    }

    @Scheduled(fixedRate = 2000)
    public void broadcastUpdates() {
        // Broadcast workers
        messagingTemplate.convertAndSend("/topic/workers", workerRegistry.getAllWorkers());

        // Broadcast system status
        SystemStatus status = new SystemStatus();
        status.setCoordinatorId(electionService.getCurrentCoordinator());
        status.setCoordinatorNodeId(electionService.getCoordinatorNodeId());
        status.setTotalWorkers(workerRegistry.getAllWorkers().size());
        status.setOnlineWorkers(workerRegistry.getOnlineWorkerCount());
        
        List<Job> activeJobs = taskScheduler.getAllJobs();
        int activeJobsCount = 0;
        int completedTasks = 0;
        int pendingTasks = 0;
        int runningTasks = 0;
        
        if (activeJobs != null) {
            for (Job job : activeJobs) {
                activeJobsCount++;
                List<Task> tasks = taskScheduler.getTasksByJob(job.getId());
                if (tasks != null) {
                    for (Task task : tasks) {
                        if (task.getStatus() == TaskStatus.COMPLETED) {
                            completedTasks++;
                        } else if (task.getStatus() == TaskStatus.PENDING) {
                            pendingTasks++;
                        } else if (task.getStatus() == TaskStatus.RUNNING) {
                            runningTasks++;
                        }
                    }
                }
            }
        }
        
        status.setActiveJobs(activeJobsCount);
        status.setCompletedTasks(completedTasks);
        status.setPendingTasks(pendingTasks);
        status.setRunningTasks(runningTasks);
        status.setMode(appConfig.getMode());
        status.setReplicationState(replicationManager.getReplicationStatus());
        
        messagingTemplate.convertAndSend("/topic/system", status);

        // Broadcast tasks (last 50)
        List<Task> allTasks = taskScheduler.getAllTasks();
        if (allTasks != null) {
            List<Task> last50Tasks = allTasks.stream()
                    .skip(Math.max(0, allTasks.size() - 50))
                    .collect(Collectors.toList());
            messagingTemplate.convertAndSend("/topic/tasks", last50Tasks);
        }

        // Broadcast events (last 50)
        List<LamportEvent> allEvents = eventLogger.getEvents();
        if (allEvents != null) {
            List<LamportEvent> last50Events = allEvents.stream()
                    .skip(Math.max(0, allEvents.size() - 50))
                    .collect(Collectors.toList());
            messagingTemplate.convertAndSend("/topic/events", last50Events);
        }

        // Broadcast logs (last 100)
        List<com.dclab.model.LogEntry> allLogs = logService.getLogs();
        if (allLogs != null) {
            List<com.dclab.model.LogEntry> last100Logs = allLogs.stream()
                    .skip(Math.max(0, allLogs.size() - 100))
                    .collect(Collectors.toList());
            messagingTemplate.convertAndSend("/topic/logs", last100Logs);
        }

        // Broadcast load balancing
        messagingTemplate.convertAndSend("/topic/load-balancing", loadBalancer.getStatus());

        // Broadcast replication
        messagingTemplate.convertAndSend("/topic/replication", replicationManager.getReplicationStatus());

        // Broadcast election
        Map<String, Object> electionStatus = new HashMap<>();
        electionStatus.put("coordinator", electionService.getCurrentCoordinator());
        electionStatus.put("coordinatorNodeId", electionService.getCoordinatorNodeId());
        electionStatus.put("electionLog", electionService.getElectionLog());
        electionStatus.put("workers", workerRegistry.getAllWorkers());
        messagingTemplate.convertAndSend("/topic/election", electionStatus);
    }
}
