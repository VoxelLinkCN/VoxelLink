package dev.voxellink.scheduler;

import org.graalvm.polyglot.HostAccess;

public interface ScheduledTask {
    @HostAccess.Export
    void cancel();
}
