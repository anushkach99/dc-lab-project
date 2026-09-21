package com.dclab.rmi;

import com.dclab.model.ElectionMessage;
import com.dclab.model.LamportEvent;
import com.dclab.model.SystemStatus;
import com.dclab.model.Task;
import com.dclab.model.TaskResult;
import com.dclab.model.WorkerInfo;
import com.dclab.service.WorkerRegistryService;
import com.dclab.service.TaskSchedulerService;
import com.dclab.election.ElectionService;
import com.dclab.clock.EventLogger;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

@Service
public class SchedulerServiceImpl extends UnicastRemoteObject implements SchedulerService {
    
    private final WorkerRegistryService workerRegistryService;
    private final TaskSchedulerService taskSchedulerService;
    private final ElectionService electionService;
    private final EventLogger eventLogger;
    
    public SchedulerServiceImpl(WorkerRegistryService workerRegistryService, 
                                @Lazy TaskSchedulerService taskSchedulerService, 
                                @Lazy ElectionService electionService, 
                                EventLogger eventLogger) throws RemoteException {
        super();
        this.workerRegistryService = workerRegistryService;
        this.taskSchedulerService = taskSchedulerService;
        this.electionService = electionService;
        this.eventLogger = eventLogger;
    }

    @Override
    public boolean registerWorker(WorkerInfo worker) throws RemoteException {
        workerRegistryService.registerWorker(worker);
        return true;
    }

    @Override
    public Task getAssignedTask(String workerId) throws RemoteException {
        return taskSchedulerService.getNextTask(workerId);
    }

    @Override
    public void reportTaskCompletion(TaskResult result) throws RemoteException {
        taskSchedulerService.completeTask(result);
    }

    @Override
    public void reportHeartbeat(String workerId, double cpuUtilization, int activeTasks, int completedTasks, double throughput) throws RemoteException {
        workerRegistryService.updateHeartbeat(workerId, cpuUtilization, activeTasks, completedTasks, throughput);
    }

    @Override
    public SystemStatus getSchedulerStatus() throws RemoteException {
        // Implement method logic to return status (assuming a getStatus method is available)
        return new SystemStatus();
    }

    @Override
    public void sendElectionMessage(ElectionMessage message) throws RemoteException {
        electionService.handleElectionMessage(message);
    }

    @Override
    public List<LamportEvent> getEvents() throws RemoteException {
        return eventLogger.getEvents();
    }
}
