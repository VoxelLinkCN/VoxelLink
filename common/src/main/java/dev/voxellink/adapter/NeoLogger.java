package dev.voxellink.adapter;

import org.graalvm.polyglot.HostAccess;

public interface NeoLogger {
    @HostAccess.Export
    void info(String message);
    @HostAccess.Export
    void warn(String message);
    @HostAccess.Export
    void error(String message);
    @HostAccess.Export
    void error(String message, Throwable throwable);
    @HostAccess.Export
    void debug(String message);
    @HostAccess.Export
    void trace(String message);
}
