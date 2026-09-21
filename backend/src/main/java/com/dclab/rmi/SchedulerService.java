package com.dclab.rmi;

import com.dclab.model.ElectionMessage;
import com.dclab.model.LamportEvent;
import com.dclab.model.SystemStatus;
import com.dclab.model.Task;
import com.dclab.model.TaskResult;
import com.dclab.model.WorkerInfo;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface SchedulerService extends Remote {
    boolean registerWorker(WorkerInfo worker) throws RemoteException;
    Task getAssignedTask(String workerId) throws RemoteException;
    void reportTaskCompletion(TaskResult result) throws RemoteException;
    void reportHeartbeat(String workerId, double cpuUtilization, int activeTasks, int completedTasks, double throughput) throws RemoteException;
    SystemStatus getSchedulerStatus() throws RemoteException;
    void sendElectionMessage(ElectionMessage message) throws RemoteException;
    List<LamportEvent> getEvents() throws RemoteException;
}
