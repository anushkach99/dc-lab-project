package com.dclab.model;

public enum TaskComplexity {
    LOW(1000000),
    MEDIUM(5000000),
    HIGH(20000000);

    private final int computationSize;

    TaskComplexity(int computationSize) {
        this.computationSize = computationSize;
    }

    public int getComputationSize() {
        return computationSize;
    }
}
