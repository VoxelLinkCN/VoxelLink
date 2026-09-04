package dev.voxellink.adapter;

import com.velocitypowered.api.scheduler.ScheduledTask;
import org.graalvm.polyglot.HostAccess;

import java.util.HashSet;
import java.util.Set;

public class VelocitySchedulerTask implements dev.voxellink.scheduler.ScheduledTask {
    private final ScheduledTask scheduledTask;
    private static final Set<ScheduledTask> tasks = new HashSet<>();

    public VelocitySchedulerTask(ScheduledTask scheduledTask) {
        this.scheduledTask = scheduledTask;
        tasks.add(scheduledTask);
    }

    @HostAccess.Export
    @Override
    public void cancel() {
        scheduledTask.cancel();
    }

    public static void cancelAllTasks() {
        tasks.forEach(ScheduledTask::cancel);
        tasks.clear();
    }
}
