package com.dclab.api;

import com.dclab.election.ElectionService;
import com.dclab.model.ElectionAlgorithm;
import com.dclab.service.WorkerRegistryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/election")
@CrossOrigin(origins = "*")
public class ElectionController {

    private final ElectionService electionService;
    private final WorkerRegistryService workerRegistry;

    @Autowired
    public ElectionController(ElectionService electionService, WorkerRegistryService workerRegistry) {
        this.electionService = electionService;
        this.workerRegistry = workerRegistry;
    }

    @GetMapping("/status")
    public Map<String, Object> getStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("coordinator", electionService.getCurrentCoordinator());
        status.put("coordinatorId", electionService.getCurrentCoordinator());
        status.put("coordinatorNodeId", electionService.getCoordinatorNodeId());
        status.put("electionLog", electionService.getElectionLog());
        status.put("events", electionService.getElectionLog());
        status.put("workers", workerRegistry.getAllWorkers());
        return status;
    }

    @PostMapping("/start")
    public ResponseEntity<String> startElection(@RequestBody Map<String, String> body) {
        String algoStr = body.get("algorithm");
        if (algoStr != null) {
            try {
                ElectionAlgorithm algorithm = ElectionAlgorithm.valueOf(algoStr.toUpperCase());
                electionService.startElection(algorithm);
                return ResponseEntity.ok("Election started successfully");
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().body("Invalid algorithm");
            }
        }
        return ResponseEntity.badRequest().body("Algorithm not provided");
    }

    @PostMapping("/fail-master")
    public ResponseEntity<String> failMaster() {
        electionService.simulateMasterFailure();
        return ResponseEntity.ok("Master failure simulated successfully");
    }
}
