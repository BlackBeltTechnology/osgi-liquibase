package liquibase.ext.logging;

import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.logging.LogLevel;
import liquibase.logging.core.AbstractLogger;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.List;

/**
 * Liquibase finds this class by itself by doing a custom component scan (sl4fj wasn't generic enough).
 */
@Slf4j
public class LiquibaseLogger extends AbstractLogger {
    public static final String FORMAT = "{} {}";
    private String name = "";
    private List<LogLevel> levels = Arrays.asList(LogLevel.SEVERE, LogLevel.WARNING, LogLevel.INFO, LogLevel.DEBUG, LogLevel.OFF);
    private LogLevel currentLogLevel = LogLevel.INFO;

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public void severe(String message) {
        if (levels.indexOf(LogLevel.SEVERE) <= levels.indexOf(currentLogLevel)) {
            log.error(FORMAT, name, message);
        }
    }

    @Override
    public void severe(String message, Throwable e) {
        if (levels.indexOf(LogLevel.SEVERE) <= levels.indexOf(currentLogLevel)) {
            log.error(FORMAT, name, message, e);
        }
    }

    @Override
    public void warning(String message) {
        if (levels.indexOf(LogLevel.WARNING) <= levels.indexOf(currentLogLevel)) {
            log.warn(FORMAT, name, message);
        }
    }

    @Override
    public void warning(String message, Throwable e) {
        if (levels.indexOf(LogLevel.WARNING) <= levels.indexOf(currentLogLevel)) {
            log.warn(FORMAT, name, message, e);
        }
    }

    @Override
    public void info(String message) {
        if (levels.indexOf(LogLevel.INFO) <= levels.indexOf(currentLogLevel)) {
            log.info(FORMAT, name, message);
        }
    }

    @Override
    public void info(String message, Throwable e) {
        if (levels.indexOf(LogLevel.INFO) <= levels.indexOf(currentLogLevel)) {
            log.info(FORMAT, name, message, e);
        }
    }

    @Override
    public void debug(String message) {
        if (levels.indexOf(LogLevel.DEBUG) <= levels.indexOf(currentLogLevel)) {
            log.debug(FORMAT, name, message);
        }
    }

    @Override
    public void debug(String message, Throwable e) {
        if (levels.indexOf(LogLevel.DEBUG) <= levels.indexOf(currentLogLevel)) {
            log.debug(FORMAT, message, e);
        }
    }

    @Override
    public void setLogLevel(String logLevel, String logFile) {
        currentLogLevel = LogLevel.valueOf(logLevel);
    }

    @Override
    public void setChangeLog(DatabaseChangeLog databaseChangeLog) {
    }

    @Override
    public void setChangeSet(ChangeSet changeSet) {
    }

    @Override
    public int getPriority() {
        return Integer.MAX_VALUE;
    }
}
