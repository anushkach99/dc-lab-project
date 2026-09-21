package com.dclab.config;

import com.dclab.demo.DemoModeManager;
import com.dclab.rmi.RmiServer;
import com.dclab.service.SystemLogService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class StartupRunner implements CommandLineRunner {

    private final AppConfig appConfig;
    private final DemoModeManager demoModeManager;
    private final SystemLogService systemLogService;
    private final RmiServer rmiServer;

    public StartupRunner(AppConfig appConfig, DemoModeManager demoModeManager, SystemLogService systemLogService, RmiServer rmiServer) {
        this.appConfig = appConfig;
        this.demoModeManager = demoModeManager;
        this.systemLogService = systemLogService;
        this.rmiServer = rmiServer;
    }

    @Override
    public void run(String... args) throws Exception {
        systemLogService.log("SYSTEM", "System starting up in " + appConfig.getMode() + " mode");
        
        if ("demo".equalsIgnoreCase(appConfig.getMode())) {
            demoModeManager.startDemoMode();
            systemLogService.log("DEMO", "Demo mode started with simulated workers");
        }
        
        // RmiServer is already started via @PostConstruct, but we log it
        systemLogService.log("SYSTEM", "RMI server ready on port " + appConfig.getRmiPort());
    }
}
