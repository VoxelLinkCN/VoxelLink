package dev.voxellink;

import dev.voxellink.adapter.FoliaScheduledTask;
import dev.voxellink.scheduler.ScheduledTask;
import org.bukkit.Bukkit;
import org.graalvm.polyglot.HostAccess;

import java.util.concurrent.TimeUnit;

public class VoxelLinkFolia extends VoxelLinkBukkit {
    @Override
    public ScheduledTask submit(Runnable task) {
        return new FoliaScheduledTask(Bukkit.getGlobalRegionScheduler().run(this, (task1) -> task.run()));
    }

    @Override
    public ScheduledTask submitAsync(Runnable task) {
        return new FoliaScheduledTask(Bukkit.getAsyncScheduler().runNow(this, (task1) -> task.run()));
    }

    @Override
    public ScheduledTask submit(Runnable task, long delay) {
        return new FoliaScheduledTask(Bukkit.getGlobalRegionScheduler().runDelayed(this, (task1) -> task.run(),
                delay * 20));
    }

    @Override
    public ScheduledTask submitAsync(Runnable task, long delay) {
        return new FoliaScheduledTask(Bukkit.getAsyncScheduler().runDelayed(this, (task1) -> task.run(),
                delay, TimeUnit.SECONDS));
    }

    @Override
    public ScheduledTask submit(Runnable task, long delay, long period) {
        return new FoliaScheduledTask(Bukkit.getGlobalRegionScheduler().runAtFixedRate(this, (task1) -> task.run(),
                delay * 20, period * 20));
    }

    @Override
    public ScheduledTask submitAsync(Runnable task, long delay, long period) {
        return new FoliaScheduledTask(Bukkit.getAsyncScheduler().runAtFixedRate(this, (task1) -> task.run(),
                delay, period, TimeUnit.SECONDS));
    }

    @Override
    public void cancelAllTasks() {
        FoliaScheduledTask.cancelAll(this);
    }
}
