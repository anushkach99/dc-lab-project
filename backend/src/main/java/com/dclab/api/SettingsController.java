package com.dclab.api;

import com.dclab.config.AppConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/settings")
@CrossOrigin(origins = "*")
public class SettingsController {

    private final AppConfig appConfig;

    @Autowired
    public SettingsController(AppConfig appConfig) {
        this.appConfig = appConfig;
    }

    @GetMapping("/")
    public Map<String, Object> getSettings() {
        return buildSettingsMap();
    }

    @PutMapping("/")
    public Map<String, Object> updateSettings(@RequestBody Map<String, Object> body) {
        return buildSettingsMap();
    }

    private Map<String, Object> buildSettingsMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("mode", appConfig.getMode());
        map.put("masterIp", appConfig.getMasterIp());
        map.put("rmiPort", appConfig.getRmiPort());
        map.put("stragglerThreshold", appConfig.getStragglerThreshold());
        map.put("maxStaleness", appConfig.getMaxStaleness());
        map.put("heartbeatInterval", appConfig.getHeartbeatInterval());
        return map;
    }
}
