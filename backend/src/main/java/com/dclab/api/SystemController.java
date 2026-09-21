package com.dclab.api;

import com.dclab.config.AppConfig;
import com.dclab.election.ElectionService;
import com.dclab.model.Job;
import com.dclab.model.SystemStatus;
import com.dclab.model.Task;
import com.dclab.model.TaskStatus;
import com.dclab.replication.ReplicationManager;
import com.dclab.service.TaskSchedulerService;
import com.dclab.service.WorkerRegistryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/system")
@CrossOrigin(origins = "*")
public class SystemController {

    private final ElectionService electionService;
    private final WorkerRegistryService workerRegistry;
    private final TaskSchedulerService taskScheduler;
    private final AppConfig appConfig;
    private final ReplicationManager replicationManager;

    @Autowired
    public SystemController(ElectionService electionService, WorkerRegistryService workerRegistry,
                            TaskSchedulerService taskScheduler, AppConfig appConfig,
                            ReplicationManager replicationManager) {
        this.electionService = electionService;
        this.workerRegistry = workerRegistry;
        this.taskScheduler = taskScheduler;
        this.appConfig = appConfig;
        this.replicationManager = replicationManager;
    }

    @GetMapping("/status")
    public SystemStatus getStatus() {
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
        return status;
    }
}
