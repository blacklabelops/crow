package com.blacklabelops.crow.dispatcher;

public enum DispatcherResult {
    EXECUTED,
    DROPPED_NOT_FOUND,
    DROPPED_ALREADY_RUNNING;
}
