package com.blacklabelops.crow.definition;

public enum ErrorMode {
    STOP,
    CONTINUE;

    public static ErrorMode getMode(String errorMode) {
        ErrorMode mode = ErrorMode.CONTINUE;
        if (errorMode != null) {
            ErrorMode foundMode = ErrorMode.valueOf(errorMode.toUpperCase());
            if (foundMode != null) {
                mode = foundMode;
            }
        }
        return mode;
    }
}
