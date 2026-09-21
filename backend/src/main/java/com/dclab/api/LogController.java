package com.dclab.api;

import com.dclab.service.SystemLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/logs")
@CrossOrigin(origins = "*")
public class LogController {

    private final SystemLogService logService;

    @Autowired
    public LogController(SystemLogService logService) {
        this.logService = logService;
    }

    @GetMapping("/")
    public List<String> getLogs() {
        return logService.getLogs();
    }

    @GetMapping("/since/{fromIndex}")
    public List<String> getLogsSince(@PathVariable int fromIndex) {
        return logService.getLogsSince(fromIndex);
    }
}
