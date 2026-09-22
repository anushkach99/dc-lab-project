package com.dclab;

import com.dclab.worker.WorkerNode;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DcLabApplication {

    public static void main(String[] args) {
        // Check if command line argument specifies worker role
        boolean isWorker = false;
        if (args != null) {
            for (String arg : args) {
                if ("--role=worker".equalsIgnoreCase(arg.trim()) || "worker".equalsIgnoreCase(arg.trim())) {
                    isWorker = true;
                    break;
                }
            }
        }

        String envRole = System.getenv("ROLE");
        if ("worker".equalsIgnoreCase(envRole)) {
            isWorker = true;
        }

        if (isWorker) {
            // Launch standalone worker node without Spring overhead
            WorkerNode.main(args);
        } else {
            // Launch Spring Boot Master Scheduler
            SpringApplication.run(DcLabApplication.class, args);
        }
    }
}
