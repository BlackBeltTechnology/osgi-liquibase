package hu.blackbelt.osgi.liquibase.logging;

/*-
 * #%L
 * OSGi liquibase
 * %%
 * Copyright (C) 2018 - 2022 BlackBelt Technology
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */


import liquibase.logging.LogMessageFilter;
import liquibase.logging.core.AbstractLogger;
import org.slf4j.Logger;

import java.util.logging.Level;
/**
 * <p>An implementation of the Liquibase Logger that sends log output to SLF4J.</p>
 *
 * <p><strong>Log level Mappings:</strong>
 *  <table summary="log level mappings">
 *     <tr>
 *         <th>Liquibase Level</th>
 *         <th>SLF4J Level</th>
 *     </tr>
 *     <tr>
 *         <td>Debug</td>
 *         <td>Debug</td>
 *     </tr>
 *     <tr>
 *         <td>Fine</td>
 *         <td>Debug</td>
 *     </tr>
 *     <tr>
 *         <td>Config</td>
 *         <td>Info</td>
 *     </tr>
 *     <tr>
 *         <td>Info</td>
 *         <td>Info</td>
 *     </tr>
 *     <tr>
 *         <td>Warning</td>
 *         <td>Warn</td>
 *     </tr>
 *     <tr>
 *         <td>Severe</td>
 *         <td>Error</td>
 *     </tr>
 * </table>
 *
 * @see liquibase.logging.Logger
 */
public class Slf4jLogger extends AbstractLogger {

    private static final int TRACE_THRESHOLD = Level.FINEST.intValue();
    private static final int DEBUG_THRESHOLD = Level.FINE.intValue();
    private static final int INFO_THRESHOLD = Level.INFO.intValue();
    private static final int WARN_THRESHOLD = Level.WARNING.intValue();

    private final Logger logger;

    Slf4jLogger(Logger logger, LogMessageFilter filter) {
        super(filter);
        this.logger = logger;
    }

    @Override
    public void log(Level level, String message, Throwable e) {
        String filteredMessage = filterMessage(message);
        int levelValue = level.intValue();
        if (levelValue <= TRACE_THRESHOLD) {
            logger.trace(filteredMessage, e);
        } else if (levelValue <= DEBUG_THRESHOLD) {
            logger.debug(filteredMessage, e);
        } else if (levelValue <= INFO_THRESHOLD) {
            logger.info(filteredMessage, e);
        } else if (levelValue <= WARN_THRESHOLD) {
            logger.warn(filteredMessage, e);
        } else {
            logger.error(filteredMessage, e);
        }
    }

    /**
     * Logs an severe message. Calls SLF4J {@link Logger#error(String)}.
     *
     * @param message The message to log.
     */
    @Override
    public void severe(String message) {
        if (logger.isErrorEnabled()) {
            logger.error(filterMessage(message));
        }
    }

    /**
     * Logs a severe message. Calls SLF4J {@link Logger#error(String, Throwable)}.
     *
     * @param message The message to log
     * @param e The exception to log.
     */
    @Override
    public void severe(String message, Throwable e) {
        if (logger.isErrorEnabled()) {
            logger.error(filterMessage(message), e);
        }
    }

    /**
     * Logs a warning message. Calls SLF4J {@link Logger#warn(String)}
     *
     * @param message The message to log.
     */
    @Override
    public void warning(String message) {
        if (logger.isWarnEnabled()) {
            logger.warn(filterMessage(message));
        }
    }

    /**
     * Logs a warning message. Calls SLF4J {@link Logger#warn(String, Throwable)}.
     *
     * @param message The message to log.
     * @param e The exception to log.
     */
    @Override
    public void warning(String message, Throwable e) {
        if (logger.isWarnEnabled()) {
            logger.warn(filterMessage(message), e);
        }
    }

    /**
     * Log an info message. Calls SLF4J {@link Logger#info(String)}.
     *
     * @param message The message to log.
     */
    @Override
    public void info(String message) {
        if (logger.isInfoEnabled()) {
            logger.info(filterMessage(message));
        }
    }

    /**
     * Log an info message. Calls SLF4J {@link Logger#info(String, Throwable)}.
     *
     * @param message The message to log.
     * @param e The exception to log.
     */
    @Override
    public void info(String message, Throwable e) {
        if (logger.isInfoEnabled()) {
            logger.info(filterMessage(message), e);
        }
    }

    /**
     * Log a config message. Calls SLF4J {@link Logger#info(String)}.
     *
     * @param message The message to log.
     */
    @Override
    public void config(String message) {
        if (logger.isInfoEnabled()) {
            logger.info(filterMessage(message));
        }
    }

    /**
     * Log a config message. Calls SLF4J {@link Logger#info(String, Throwable)}.
     *
     * @param message The message to log
     * @param e The exception to log
     */
    @Override
    public void config(String message, Throwable e) {
        if (logger.isInfoEnabled()) {
            logger.info(filterMessage(message), e);
        }
    }

    /**
     * Log a fine message. Calls SLF4J {@link Logger#debug(String)}.
     *
     * @param message The message to log.
     */
    @Override
    public void fine(String message) {
        if (logger.isDebugEnabled()) {
            logger.debug(filterMessage(message));
        }
    }

    /**
     * Log a fine message. Calls SLF4J {@link Logger#debug(String, Throwable)}.
     *
     * @param message The message to log.
     * @param e The exception to log.
     */
    @Override
    public void fine(String message, Throwable e) {
        if (logger.isDebugEnabled()) {
            logger.debug(filterMessage(message), e);
        }
    }
}
