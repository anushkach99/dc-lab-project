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

    private String currentMode;
    private String masterIp;
    private int rmiPort;
    private double stragglerThreshold;
    private long maxStaleness;
    private long heartbeatInterval;
    private int defaultTaskCount = 100;
    private String defaultComplexity = "MEDIUM";

    @Autowired
    public SettingsController(AppConfig appConfig) {
        this.appConfig = appConfig;
        this.currentMode = appConfig.getMode() != null ? appConfig.getMode().toUpperCase() : "DEMO";
        this.masterIp = appConfig.getMasterIp();
        this.rmiPort = appConfig.getRmiPort();
        this.stragglerThreshold = appConfig.getStragglerThreshold();
        this.maxStaleness = appConfig.getMaxStaleness();
        this.heartbeatInterval = appConfig.getHeartbeatInterval();
    }

    @GetMapping({"", "/"})
    public Map<String, Object> getSettings() {
        return buildSettingsMap();
    }

    @PutMapping({"", "/"})
    @SuppressWarnings("unchecked")
    public Map<String, Object> updateSettings(@RequestBody Map<String, Object> body) {
        if (body != null) {
            if (body.containsKey("mode")) {
                this.currentMode = String.valueOf(body.get("mode")).toUpperCase();
            }
            if (body.get("network") instanceof Map) {
                Map<String, Object> net = (Map<String, Object>) body.get("network");
                if (net.containsKey("masterIp")) this.masterIp = String.valueOf(net.get("masterIp"));
                if (net.containsKey("rmiPort")) this.rmiPort = ((Number) net.get("rmiPort")).intValue();
            }
            if (body.get("scheduling") instanceof Map) {
                Map<String, Object> sch = (Map<String, Object>) body.get("scheduling");
                if (sch.containsKey("stragglerThreshold")) this.stragglerThreshold = ((Number) sch.get("stragglerThreshold")).doubleValue();
                if (sch.containsKey("maxStaleness")) this.maxStaleness = ((Number) sch.get("maxStaleness")).longValue();
                if (sch.containsKey("heartbeatInterval")) this.heartbeatInterval = ((Number) sch.get("heartbeatInterval")).longValue();
            }
            if (body.get("jobConfig") instanceof Map) {
                Map<String, Object> jc = (Map<String, Object>) body.get("jobConfig");
                if (jc.containsKey("defaultTaskCount")) this.defaultTaskCount = ((Number) jc.get("defaultTaskCount")).intValue();
                if (jc.containsKey("defaultComplexity")) this.defaultComplexity = String.valueOf(jc.get("defaultComplexity"));
            }
        }
        return buildSettingsMap();
    }

    private Map<String, Object> buildSettingsMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("mode", currentMode);

        Map<String, Object> network = new HashMap<>();
        network.put("masterIp", masterIp);
        network.put("workerIps", "LAN / DHCP");
        network.put("rmiPort", rmiPort);
        network.put("restPort", 8080);
        map.put("network", network);

        Map<String, Object> scheduling = new HashMap<>();
        scheduling.put("stragglerThreshold", stragglerThreshold);
        scheduling.put("maxStaleness", maxStaleness);
        scheduling.put("heartbeatInterval", heartbeatInterval);
        map.put("scheduling", scheduling);

        Map<String, Object> jobConfig = new HashMap<>();
        jobConfig.put("defaultTaskCount", defaultTaskCount);
        jobConfig.put("defaultComplexity", defaultComplexity);
        map.put("jobConfig", jobConfig);

        // Flat aliases for backwards compatibility
        map.put("masterIp", masterIp);
        map.put("rmiPort", rmiPort);
        map.put("stragglerThreshold", stragglerThreshold);
        map.put("maxStaleness", maxStaleness);
        map.put("heartbeatInterval", heartbeatInterval);

        return map;
    }
}
