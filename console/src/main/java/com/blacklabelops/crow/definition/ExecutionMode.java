package com.blacklabelops.crow.definition;

public enum ExecutionMode {
    SEQUENTIAL,
    PARALLEL;

    public static ExecutionMode getMode(String executionMode) {
        ExecutionMode mode = ExecutionMode.SEQUENTIAL;
        if (executionMode != null) {
            ExecutionMode foundMode = ExecutionMode.valueOf(executionMode.toUpperCase());
            if (foundMode != null) {
                mode = foundMode;
            }
        }
        return mode;
    }
}
