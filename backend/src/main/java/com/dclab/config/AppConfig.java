package com.dclab.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    @Value("${app.mode:demo}")
    private String mode;

    @Value("${app.master.ip:localhost}")
    private String masterIp;

    @Value("${app.master.rmi-port:1099}")
    private int rmiPort;

    @Value("${app.master.node-id:100}")
    private int masterNodeId;

    @Value("${app.straggler-threshold:0.6}")
    private double stragglerThreshold;

    @Value("${app.max-staleness:10}")
    private long maxStaleness;

    @Value("${app.heartbeat-interval:3000}")
    private long heartbeatInterval;

    @Value("${app.replication-interval:5000}")
    private long replicationInterval;

    public String getMode() { return mode; }
    public String getMasterIp() { return masterIp; }
    public int getRmiPort() { return rmiPort; }
    public int getMasterNodeId() { return masterNodeId; }
    public double getStragglerThreshold() { return stragglerThreshold; }
    public long getMaxStaleness() { return maxStaleness; }
    public long getHeartbeatInterval() { return heartbeatInterval; }
    public long getReplicationInterval() { return replicationInterval; }
}
