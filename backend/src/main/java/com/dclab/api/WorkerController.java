package com.dclab.api;

import com.dclab.model.WorkerInfo;
import com.dclab.service.WorkerRegistryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/workers")
@CrossOrigin(origins = "*")
public class WorkerController {

    private final WorkerRegistryService workerRegistry;

    @Autowired
    public WorkerController(WorkerRegistryService workerRegistry) {
        this.workerRegistry = workerRegistry;
    }

    @GetMapping("/")
    public List<WorkerInfo> getAllWorkers() {
        return workerRegistry.getAllWorkers();
    }

    @GetMapping("/{id}")
    public ResponseEntity<WorkerInfo> getWorker(@PathVariable String id) {
        WorkerInfo worker = workerRegistry.getWorker(id);
        if (worker != null) {
            return ResponseEntity.ok(worker);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/register")
    public ResponseEntity<String> registerWorker(@RequestBody WorkerInfo worker) {
        workerRegistry.registerWorker(worker);
        return ResponseEntity.ok("Worker registered successfully");
    }
}
