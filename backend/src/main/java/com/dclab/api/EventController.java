package com.dclab.api;

import com.dclab.clock.EventLogger;
import com.dclab.model.LamportEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/events")
@CrossOrigin(origins = "*")
public class EventController {

    private final EventLogger eventLogger;

    @Autowired
    public EventController(EventLogger eventLogger) {
        this.eventLogger = eventLogger;
    }

    @GetMapping("")
    public List<LamportEvent> getEvents() {
        return eventLogger.getEvents();
    }

    @GetMapping("/since/{timestamp}")
    public List<LamportEvent> getEventsSince(@PathVariable long timestamp) {
        return eventLogger.getEventsSince(timestamp);
    }
}
