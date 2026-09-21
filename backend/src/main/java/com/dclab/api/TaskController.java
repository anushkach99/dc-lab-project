package com.dclab.api;

import com.dclab.model.Task;
import com.dclab.service.TaskSchedulerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@CrossOrigin(origins = "*")
public class TaskController {

    private final TaskSchedulerService taskScheduler;

    @Autowired
    public TaskController(TaskSchedulerService taskScheduler) {
        this.taskScheduler = taskScheduler;
    }

    @GetMapping("/")
    public List<Task> getAllTasks() {
        return taskScheduler.getAllTasks();
    }

    @GetMapping("/job/{jobId}")
    public List<Task> getTasksByJob(@PathVariable String jobId) {
        return taskScheduler.getTasksByJob(jobId);
    }
}
