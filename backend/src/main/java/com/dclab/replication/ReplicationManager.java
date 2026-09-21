package com.dclab.replication;

import com.dclab.model.ReplicationState;
import com.dclab.model.SchedulerState;
import org.springframework.stereotype.Service;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class ReplicationManager {
    private SchedulerState primaryState;
    private SchedulerState backupState;
    private long maxStaleness = 10;
    private final AtomicLong primaryVersion = new AtomicLong(0);
    private final AtomicLong backupVersion = new AtomicLong(0);
    private long lastReplicationTime = System.currentTimeMillis();
    private boolean simulateDelay = false;
    private final ScheduledExecutorService replicationExecutor = Executors.newSingleThreadScheduledExecutor();

    public ReplicationManager() {
        replicationExecutor.scheduleAtFixedRate(this::checkReplication, 5000, 5000, TimeUnit.MILLISECONDS);
    }

    private void checkReplication() {
        if (!simulateDelay && getStaleness() > maxStaleness) {
            triggerSync();
        }
    }

    public void updatePrimaryState(SchedulerState state) {
        this.primaryState = state;
        primaryVersion.incrementAndGet();
    }

    public void triggerSync() {
        this.backupState = this.primaryState;
        this.backupVersion.set(this.primaryVersion.get());
        this.lastReplicationTime = System.currentTimeMillis();
    }

    public ReplicationState getReplicationStatus() {
        long staleness = getStaleness();
        String status = staleness == 0 ? "SYNCED" : (simulateDelay ? "DELAYED" : "SYNCING");
        return new ReplicationState(primaryVersion.get(), backupVersion.get(), lastReplicationTime, staleness, status, (int)staleness);
    }

    public void simulateReplicaDelay(boolean enable) {
        this.simulateDelay = enable;
    }

    public SchedulerState getPrimaryState() {
        return primaryState;
    }

    public SchedulerState getBackupState() {
        return backupState;
    }

    public long getStaleness() {
        return primaryVersion.get() - backupVersion.get();
    }
}
