package com.dclab.api;

import com.dclab.model.Job;
import com.dclab.model.TaskComplexity;
import com.dclab.service.TaskSchedulerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/jobs")
@CrossOrigin(origins = "*")
public class JobController {

    private final TaskSchedulerService taskScheduler;

    @Autowired
    public JobController(TaskSchedulerService taskScheduler) {
        this.taskScheduler = taskScheduler;
    }

    @GetMapping("")
    public List<Job> getAllJobs() {
        return taskScheduler.getAllJobs();
    }

    @PostMapping("")
    public ResponseEntity<Job> createJob(@RequestBody CreateJobRequest request) {
        TaskComplexity complexity;
        try {
            complexity = TaskComplexity.valueOf(request.getComplexity().toUpperCase());
        } catch (Exception e) {
            complexity = TaskComplexity.LOW; // fallback
        }
        Job job = taskScheduler.createJob(request.getNumTasks(), complexity);
        return ResponseEntity.ok(job);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Job> getJob(@PathVariable String id) {
        Job job = taskScheduler.getJob(id);
        if (job != null) {
            return ResponseEntity.ok(job);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{id}/start")
    public ResponseEntity<String> startJob(@PathVariable String id) {
        taskScheduler.startJob(id);
        return ResponseEntity.ok("Job started successfully");
    }

    public static class CreateJobRequest {
        private int numTasks;
        private String complexity;

        public int getNumTasks() {
            return numTasks;
        }

        public void setNumTasks(int numTasks) {
            this.numTasks = numTasks;
        }

        public String getComplexity() {
            return complexity;
        }

        public void setComplexity(String complexity) {
            this.complexity = complexity;
        }
    }
}
