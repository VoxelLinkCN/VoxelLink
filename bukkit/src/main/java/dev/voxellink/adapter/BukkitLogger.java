package dev.voxellink.adapter;

import org.graalvm.polyglot.HostAccess;

import java.util.logging.Logger;

public class BukkitLogger implements NeoLogger {
    private final Logger logger;

    public BukkitLogger(Logger logger) {
        this.logger = logger;
    }

    @HostAccess.Export
    @Override
    public void info(String message) {
        logger.info(message);
    }

    @HostAccess.Export
    @Override
    public void warn(String message) {
        logger.warning(message);
    }

    @HostAccess.Export
    @Override
    public void error(String message) {
        logger.severe(message);
    }

    @HostAccess.Export
    @Override
    public void error(String message, Throwable throwable) {
        logger.severe(message);
        throwable.printStackTrace();
    }

    @HostAccess.Export
    @Override
    public void debug(String message) {
        logger.fine(message);
    }

    @Override
    public void trace(String message) {
        logger.finest(message);
    }
}
