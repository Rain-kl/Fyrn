package com.arctel.oms.infrastructure.task.base;

public interface TaskLogger {

    void logInfo(String logMessage);

    void logError(String logMessage, Exception e);

}
