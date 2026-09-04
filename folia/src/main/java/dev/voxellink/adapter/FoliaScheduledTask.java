package dev.voxellink.adapter;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.graalvm.polyglot.HostAccess;

public class FoliaScheduledTask implements dev.voxellink.scheduler.ScheduledTask {
    private final ScheduledTask scheduledTask;

    public FoliaScheduledTask(ScheduledTask scheduledTask) {
        this.scheduledTask = scheduledTask;
    }

    @HostAccess.Export
    @Override
    public void cancel() {
        scheduledTask.cancel();
    }

    public static void cancelAll(Plugin plugin) {
        Bukkit.getGlobalRegionScheduler().cancelTasks(plugin);
        Bukkit.getAsyncScheduler().cancelTasks(plugin);
    }
}
