package com.dclab.rmi;

import com.dclab.config.AppConfig;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

@Component
public class RmiServer {

    private final SchedulerServiceImpl schedulerServiceImpl;
    private final AppConfig appConfig;

    public RmiServer(SchedulerServiceImpl schedulerServiceImpl, AppConfig appConfig) {
        this.schedulerServiceImpl = schedulerServiceImpl;
        this.appConfig = appConfig;
    }

    @PostConstruct
    public void start() {
        try {
            Registry registry = LocateRegistry.createRegistry(appConfig.getRmiPort());
            registry.rebind("SchedulerService", schedulerServiceImpl);
            System.out.println("RMI Server started on port " + appConfig.getRmiPort());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
