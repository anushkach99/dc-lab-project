package com.dclab.election;

import com.dclab.model.ElectionAlgorithm;
import com.dclab.model.ElectionMessage;
import com.dclab.service.WorkerRegistryService;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class ElectionService {
    private String currentCoordinator;
    private int coordinatorNodeId;
    private ElectionAlgorithm activeAlgorithm;
    private final List<ElectionEvent> electionLog = new CopyOnWriteArrayList<>();
    private boolean electionInProgress = false;
    private final WorkerRegistryService workerRegistry;

    public ElectionService(WorkerRegistryService workerRegistry) {
        this.workerRegistry = workerRegistry;
    }

    public void startElection(ElectionAlgorithm algorithm) {
        this.activeAlgorithm = algorithm;
        this.electionInProgress = true;
        // Logic to start election based on algorithm
    }

    public void handleElectionMessage(ElectionMessage message) {
        // Logic to process message
    }

    public String getCurrentCoordinator() {
        return currentCoordinator;
    }

    public int getCoordinatorNodeId() {
        return coordinatorNodeId;
    }

    public void simulateMasterFailure() {
        this.electionInProgress = true;
        startElection(activeAlgorithm != null ? activeAlgorithm : ElectionAlgorithm.BULLY);
    }

    public List<ElectionEvent> getElectionLog() {
        return electionLog;
    }

    public void setAlgorithm(ElectionAlgorithm alg) {
        this.activeAlgorithm = alg;
    }

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
