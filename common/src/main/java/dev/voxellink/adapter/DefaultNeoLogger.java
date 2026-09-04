package dev.voxellink.adapter;

import org.slf4j.Logger;


public class DefaultNeoLogger implements dev.voxellink.adapter.NeoLogger {
    private final Logger logger;

    public DefaultNeoLogger(Logger logger) {
        this.logger = logger;
    }

    @Override
    public void info(String message) {
        logger.info(message);
    }

    @Override
    public void debug(String message) {
        logger.debug(message);
    }

    @Override
    public void error(String message, Throwable throwable) {
        logger.error(message, throwable);
    }

    @Override
    public void error(String message) {
        logger.error(message);
    }

    @Override
    public void warn(String message) {
        logger.warn(message);
    }

    @Override
    public void trace(String message) {
        logger.trace(message);
    }
}
