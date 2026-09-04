package dev.voxellink.scheduler;

import dev.voxellink.script.ScriptProvider;
import org.graalvm.polyglot.HostAccess;

public interface SchedulerProvider {
    @HostAccess.Export
    ScheduledTask submit(Runnable task);

    @HostAccess.Export
    ScheduledTask submitAsync(Runnable task);

    @HostAccess.Export
    ScheduledTask submit(Runnable task, long delay);

    @HostAccess.Export
    ScheduledTask submitAsync(Runnable task, long delay);

    @HostAccess.Export
    ScheduledTask submit(Runnable task, long delay, long period);

    @HostAccess.Export
    ScheduledTask submitAsync(Runnable task, long delay, long period);

    void cancelAllTasks();
}
