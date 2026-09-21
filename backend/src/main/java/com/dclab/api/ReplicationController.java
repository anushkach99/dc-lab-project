package com.dclab.api;

import com.dclab.model.ReplicationState;
import com.dclab.replication.ReplicationManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/replication")
@CrossOrigin(origins = "*")
public class ReplicationController {

    private final ReplicationManager replicationManager;

    @Autowired
    public ReplicationController(ReplicationManager replicationManager) {
        this.replicationManager = replicationManager;
    }

    @GetMapping("/status")
    public ReplicationState getStatus() {
        return replicationManager.getReplicationStatus();
    }

    @PostMapping("/sync")
    public ResponseEntity<String> triggerSync() {
        replicationManager.triggerSync();
        return ResponseEntity.ok("Sync triggered successfully");
    }

    @PostMapping("/simulate-delay")
    public ResponseEntity<String> simulateDelay(@RequestBody Map<String, Boolean> body) {
        Boolean enable = body.get("enable");
        if (enable != null) {
            replicationManager.simulateReplicaDelay(enable);
            return ResponseEntity.ok("Simulate delay set to " + enable);
        }
        return ResponseEntity.badRequest().body("enable field is missing");
    }
}
