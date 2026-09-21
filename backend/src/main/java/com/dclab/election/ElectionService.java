package com.dclab.election;

import com.dclab.clock.EventLogger;
import com.dclab.model.ElectionAlgorithm;
import com.dclab.model.ElectionMessage;
import com.dclab.model.NodeStatus;
import com.dclab.model.WorkerInfo;
import com.dclab.service.SystemLogService;
import com.dclab.service.WorkerRegistryService;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@Service
public class ElectionService {
    private String currentCoordinator = "MASTER";
    private int coordinatorNodeId = 100;
    private ElectionAlgorithm activeAlgorithm = ElectionAlgorithm.BULLY;
    private final List<ElectionEvent> electionLog = new CopyOnWriteArrayList<>();
    private boolean electionInProgress = false;
    private final WorkerRegistryService workerRegistry;
    private final SystemLogService logService;
    private final EventLogger eventLogger;

    public ElectionService(WorkerRegistryService workerRegistry, SystemLogService logService, EventLogger eventLogger) {
        this.workerRegistry = workerRegistry;
        this.logService = logService;
        this.eventLogger = eventLogger;
    }

    public synchronized void startElection(ElectionAlgorithm algorithm) {
        this.activeAlgorithm = algorithm;
        this.electionInProgress = true;
        
        logService.addLog("ELECTION", "Leader election initiated using " + algorithm + " algorithm");

        if (algorithm == ElectionAlgorithm.BULLY) {
            runBullyElection();
        } else {
            runRingElection();
        }
        
        this.electionInProgress = false;
    }

    private void runBullyElection() {
        List<WorkerInfo> onlineWorkers = workerRegistry.getOnlineWorkers();
        int initiatorId = 20; // Edge-01 detects failure
        
        addEvent("BULLY", initiatorId, -1, "ELECTION", "Node " + initiatorId + " detected coordinator failure and initiated BULLY election");

        // Initiator sends election messages to higher ID nodes
        List<WorkerInfo> higherNodes = onlineWorkers.stream()
                .filter(w -> w.getNodeId() > initiatorId)
                .sorted(Comparator.comparingInt(WorkerInfo::getNodeId))
                .collect(Collectors.toList());

        WorkerInfo winner = null;

        for (WorkerInfo w : higherNodes) {
            addEvent("BULLY", initiatorId, w.getNodeId(), "ELECTION", "Election message sent to node " + w.getNodeId());
            addEvent("BULLY", w.getNodeId(), initiatorId, "ANSWER", "Node " + w.getNodeId() + " answered OK to node " + initiatorId);
            winner = w;
        }

        if (winner == null && !onlineWorkers.isEmpty()) {
            winner = onlineWorkers.get(0);
        }

        if (winner != null) {
            this.currentCoordinator = winner.getWorkerId();
            this.coordinatorNodeId = winner.getNodeId();
            winner.setStatus(NodeStatus.COORDINATOR);

            addEvent("BULLY", winner.getNodeId(), -1, "COORDINATOR", "Node " + winner.getNodeId() + " (" + winner.getWorkerId() + ") won election and broadcast COORDINATOR status");
            logService.addLog("ELECTION", "New Coordinator elected via BULLY: " + winner.getWorkerId() + " (Node ID: " + winner.getNodeId() + ")");
            eventLogger.logEvent(winner.getWorkerId(), "NEW_COORDINATOR_ELECTED", 0, null, "Elected via Bully Algorithm");
        }
    }

    private void runRingElection() {
        List<WorkerInfo> onlineWorkers = workerRegistry.getOnlineWorkers().stream()
                .sorted(Comparator.comparingInt(WorkerInfo::getNodeId))
                .collect(Collectors.toList());

        if (onlineWorkers.isEmpty()) return;

        addEvent("RING", onlineWorkers.get(0).getNodeId(), -1, "ELECTION", "Logical ring constructed. Election message token passing started.");

        int maxNodeId = -1;
        WorkerInfo winner = null;

        for (int i = 0; i < onlineWorkers.size(); i++) {
            WorkerInfo current = onlineWorkers.get(i);
            WorkerInfo next = onlineWorkers.get((i + 1) % onlineWorkers.size());

            addEvent("RING", current.getNodeId(), next.getNodeId(), "ELECTION_TOKEN", 
                    "Node " + current.getNodeId() + " passed election token [Max: " + Math.max(maxNodeId, current.getNodeId()) + "] to Node " + next.getNodeId());

            if (current.getNodeId() > maxNodeId) {
                maxNodeId = current.getNodeId();
                winner = current;
            }
        }

        if (winner != null) {
            this.currentCoordinator = winner.getWorkerId();
            this.coordinatorNodeId = winner.getNodeId();
            winner.setStatus(NodeStatus.COORDINATOR);

            for (int i = 0; i < onlineWorkers.size(); i++) {
                WorkerInfo current = onlineWorkers.get(i);
                WorkerInfo next = onlineWorkers.get((i + 1) % onlineWorkers.size());
                addEvent("RING", current.getNodeId(), next.getNodeId(), "LEADER_TOKEN", 
                        "Node " + current.getNodeId() + " circulated Coordinator announcement (" + winner.getWorkerId() + ") to Node " + next.getNodeId());
            }

            logService.addLog("ELECTION", "New Coordinator elected via RING: " + winner.getWorkerId() + " (Node ID: " + winner.getNodeId() + ")");
            eventLogger.logEvent(winner.getWorkerId(), "NEW_COORDINATOR_ELECTED", 0, null, "Elected via Ring Algorithm");
        }
    }

    public void simulateMasterFailure() {
        logService.addLog("ELECTION", "Simulating Master Coordinator Failure (Node ID 100 OFFLINE)");
        WorkerInfo master = workerRegistry.getWorker("MASTER");
        if (master != null) master.setStatus(NodeStatus.OFFLINE);
        
        startElection(activeAlgorithm != null ? activeAlgorithm : ElectionAlgorithm.BULLY);
    }

    public void handleElectionMessage(ElectionMessage message) {
        if (message != null) {
            addEvent(message.getAlgorithm() != null ? message.getAlgorithm().name() : "BULLY", 
                     message.getSenderId(), message.getReceiverId(), message.getMessageType(), "Received election message");
        }
    }

    private void addEvent(String algo, int fromNode, int toNode, String msgType, String desc) {
        electionLog.add(new ElectionEvent(algo, fromNode, toNode, msgType, System.currentTimeMillis(), desc));
    }

    public String getCurrentCoordinator() { return currentCoordinator; }
    public int getCoordinatorNodeId() { return coordinatorNodeId; }
    public List<ElectionEvent> getElectionLog() { return electionLog; }
    public ElectionAlgorithm getActiveAlgorithm() { return activeAlgorithm; }

    public static class ElectionEvent {
        private String algorithm;
        private int fromNode;
        private int toNode;
        private String messageType;
        private long timestamp;
        private String description;
        
        public ElectionEvent(String algorithm, int fromNode, int toNode, String messageType, long timestamp, String description) {
            this.algorithm = algorithm;
            this.fromNode = fromNode;
            this.toNode = toNode;
            this.messageType = messageType;
            this.timestamp = timestamp;
            this.description = description;
        }
        
        public String getAlgorithm() { return algorithm; }
        public int getFromNode() { return fromNode; }
        public int getToNode() { return toNode; }
        public String getMessageType() { return messageType; }
        public long getTimestamp() { return timestamp; }
        public String getDescription() { return description; }
    }
}
