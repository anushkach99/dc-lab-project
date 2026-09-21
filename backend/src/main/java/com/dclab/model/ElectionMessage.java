package com.dclab.model;

import java.io.Serializable;
import java.util.List;

public class ElectionMessage implements Serializable {
    private int senderId;
    private int receiverId;
    private String messageType;
    private ElectionAlgorithm algorithm;
    private long timestamp;
    private List<Integer> candidateList;

    public ElectionMessage() {}

    public ElectionMessage(int senderId, int receiverId, String messageType, ElectionAlgorithm algorithm, long timestamp, List<Integer> candidateList) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.messageType = messageType;
        this.algorithm = algorithm;
        this.timestamp = timestamp;
        this.candidateList = candidateList;
    }

    public int getSenderId() { return senderId; }
    public void setSenderId(int senderId) { this.senderId = senderId; }
    public int getReceiverId() { return receiverId; }
    public void setReceiverId(int receiverId) { this.receiverId = receiverId; }
    public String getMessageType() { return messageType; }
    public void setMessageType(String messageType) { this.messageType = messageType; }
    public ElectionAlgorithm getAlgorithm() { return algorithm; }
    public void setAlgorithm(ElectionAlgorithm algorithm) { this.algorithm = algorithm; }
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    public List<Integer> getCandidateList() { return candidateList; }
    public void setCandidateList(List<Integer> candidateList) { this.candidateList = candidateList; }
}
