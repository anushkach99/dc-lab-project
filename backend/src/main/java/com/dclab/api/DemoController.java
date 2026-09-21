package com.dclab.api;

import com.dclab.demo.DemoModeManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/demo")
@CrossOrigin(origins = "*")
public class DemoController {

    private final DemoModeManager demoManager;

    @Autowired
    public DemoController(DemoModeManager demoManager) {
        this.demoManager = demoManager;
    }

    @PostMapping("/start")
    public ResponseEntity<String> startDemo() {
        demoManager.startDemoMode();
        return ResponseEntity.ok("Demo mode started");
    }

    @PostMapping("/stop")
    public ResponseEntity<String> stopDemo() {
        demoManager.stopDemoMode();
        return ResponseEntity.ok("Demo mode stopped");
    }

    @PostMapping("/simulate-straggler")
    public ResponseEntity<String> simulateStraggler(@RequestBody Map<String, String> body) {
        String workerId = body.get("workerId");
        if (workerId != null) {
            demoManager.simulateStraggler(workerId);
            return ResponseEntity.ok("Straggler simulated");
        }
        return ResponseEntity.badRequest().body("workerId missing");
    }

    @PostMapping("/restore-worker")
    public ResponseEntity<String> restoreWorker(@RequestBody Map<String, String> body) {
        String workerId = body.get("workerId");
        if (workerId != null) {
            demoManager.restoreWorker(workerId);
            return ResponseEntity.ok("Worker restored");
        }
        return ResponseEntity.badRequest().body("workerId missing");
    }

    @PostMapping("/run-full-experiment")
    public ResponseEntity<String> runFullExperiment() {
        new Thread(() -> {
            demoManager.runFullExperiment();
        }).start();
        return ResponseEntity.ok("Experiment started");
    }
}
