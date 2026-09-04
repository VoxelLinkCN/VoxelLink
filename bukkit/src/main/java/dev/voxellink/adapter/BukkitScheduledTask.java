package dev.voxellink.adapter;

import dev.voxellink.scheduler.ScheduledTask;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import org.graalvm.polyglot.HostAccess;

public class BukkitScheduledTask implements ScheduledTask {
    private final BukkitTask task;

    public BukkitScheduledTask(BukkitTask task) {
        this.task = task;
    }

    @HostAccess.Export
    @Override
    public void cancel() {
        task.cancel();
    }

    public static void cancelAll(Plugin plugin) {
        Bukkit.getScheduler().cancelTasks(plugin);
    }
}
