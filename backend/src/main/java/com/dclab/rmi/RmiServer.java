package com.dclab.rmi;

import com.dclab.config.AppConfig;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
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
            String masterIp = appConfig.getMasterIp();
            String envMasterIp = System.getenv("MASTER_IP");
            if (envMasterIp != null && !envMasterIp.isBlank()) {
                masterIp = envMasterIp.trim();
            }

            // If masterIp is still localhost and running in LAN mode, attempt to detect actual LAN IP
            if (("localhost".equalsIgnoreCase(masterIp) || "127.0.0.1".equals(masterIp))
                    && "lan".equalsIgnoreCase(appConfig.getMode())) {
                try {
                    masterIp = InetAddress.getLocalHost().getHostAddress();
                } catch (Exception ignored) {}
            }

            if (masterIp != null && !masterIp.isBlank()) {
                System.setProperty("java.rmi.server.hostname", masterIp);
                System.out.println("Setting java.rmi.server.hostname = " + masterIp);
            }

            Registry registry;
            try {
                registry = LocateRegistry.createRegistry(appConfig.getRmiPort());
            } catch (Exception e) {
                registry = LocateRegistry.getRegistry(appConfig.getRmiPort());
            }
            registry.rebind("SchedulerService", schedulerServiceImpl);
            System.out.println("RMI Server started on port " + appConfig.getRmiPort() + " (Export Host: " + masterIp + ")");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
