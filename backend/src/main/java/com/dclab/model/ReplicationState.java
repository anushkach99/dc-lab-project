package com.dclab.model;

public class ReplicationState {
    private long primaryVersion;
    private long backupVersion;
    private long lastReplicationTime;
    private long stalenessMs;
    private String syncStatus;
    private int pendingUpdates;

    public ReplicationState() {}

    public ReplicationState(long primaryVersion, long backupVersion, long lastReplicationTime, long stalenessMs, String syncStatus, int pendingUpdates) {
        this.primaryVersion = primaryVersion;
        this.backupVersion = backupVersion;
        this.lastReplicationTime = lastReplicationTime;
        this.stalenessMs = stalenessMs;
        this.syncStatus = syncStatus;
        this.pendingUpdates = pendingUpdates;
    }

    public long getPrimaryVersion() { return primaryVersion; }
    public void setPrimaryVersion(long primaryVersion) { this.primaryVersion = primaryVersion; }
    public long getBackupVersion() { return backupVersion; }
    public void setBackupVersion(long backupVersion) { this.backupVersion = backupVersion; }
    public long getLastReplicationTime() { return lastReplicationTime; }
    public void setLastReplicationTime(long lastReplicationTime) { this.lastReplicationTime = lastReplicationTime; }
    public long getStalenessMs() { return stalenessMs; }
    public void setStalenessMs(long stalenessMs) { this.stalenessMs = stalenessMs; }
    public String getSyncStatus() { return syncStatus; }
    public void setSyncStatus(String syncStatus) { this.syncStatus = syncStatus; }
    public int getPendingUpdates() { return pendingUpdates; }
    public void setPendingUpdates(int pendingUpdates) { this.pendingUpdates = pendingUpdates; }
}
