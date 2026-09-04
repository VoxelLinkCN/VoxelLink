package dev.voxellink.script;

import dev.voxellink.VoxelLink;
import dev.voxellink.scheduler.ScheduledTask;
import dev.voxellink.scheduler.SchedulerProvider;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.HostAccess;
import org.graalvm.polyglot.Value;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScriptScheduler {
    
    private final VoxelLink plugin;

    private final Map<ScheduledTask, Context> scriptTasks = new HashMap<>();
    
    public ScriptScheduler(VoxelLink plugin) {
        this.plugin = plugin;
    }

    @HostAccess.Export
    public ScheduledTask submit(Value task) {
        if (!task.canExecute()) return null;
        ScheduledTask scheduledTask = plugin.submit(task::execute);
        scriptTasks.put(scheduledTask, task.getContext());
        return scheduledTask;
    }

    @HostAccess.Export
    public ScheduledTask submit(Value task, long delay) {
        if (!task.canExecute()) return null;
        ScheduledTask scheduledTask = plugin.submit(task::execute, delay);
        scriptTasks.put(scheduledTask, task.getContext());
        return scheduledTask;
    }

    @HostAccess.Export
    public ScheduledTask submit(Value task, long delay, long period) {
        if (!task.canExecute()) return null;
        ScheduledTask scheduledTask = plugin.submit(task::execute, delay, period);
        scriptTasks.put(scheduledTask, task.getContext());
        return scheduledTask;
    }

    @HostAccess.Export
    public ScheduledTask submitAsync(Value task) {
        ScheduledTask scheduledTask = plugin.submitAsync(task::execute);
        scriptTasks.put(scheduledTask, task.getContext());
        return scheduledTask;
    }

    @HostAccess.Export
    public ScheduledTask submitAsync(Value task, long delay) {
        ScheduledTask scheduledTask = plugin.submitAsync(task::execute, delay);
        scriptTasks.put(scheduledTask, task.getContext());
        return scheduledTask;
    }

    @HostAccess.Export
    public ScheduledTask submitAsync(Value task, long delay, long period) {
        ScheduledTask scheduledTask = plugin.submitAsync(task::execute, delay, period);
        scriptTasks.put(scheduledTask, task.getContext());
        return scheduledTask;
    }

    @HostAccess.Export
    public ScheduledTask submit(String scriptName, String functionName, long delay) {
        ScheduledTask scheduledTask = plugin.submit(() -> {
            if (plugin.getScriptProvider().isScriptLoaded(scriptName)) {
                Value value = plugin.getScriptProvider().getScriptContext(scriptName).getBindings("js").getMember(functionName);
                if (value.canExecute()) {
                    value.execute();
                }
            }
        }, delay);
        scriptTasks.put(scheduledTask, plugin.getScriptProvider().getScriptContext(scriptName));
        return scheduledTask;
    }

    @HostAccess.Export
    public ScheduledTask submitAsync(String scriptName, String functionName, long delay) {
        ScheduledTask scheduledTask = plugin.submitAsync(() -> {
            if (plugin.getScriptProvider().isScriptLoaded(scriptName)) {
                Value value = plugin.getScriptProvider().getScriptContext(scriptName).getBindings("js").getMember(functionName);
                if (value.canExecute()) {
                    value.execute();
                }
            }
        }, delay);
        scriptTasks.put(scheduledTask, plugin.getScriptProvider().getScriptContext(scriptName));
        return scheduledTask;
    }

    @HostAccess.Export
    public ScheduledTask submit(String scriptName, String functionName, long delay, long period) {
        ScheduledTask scheduledTask = plugin.submit(() -> {
            if (plugin.getScriptProvider().isScriptLoaded(scriptName)) {
                Value value = plugin.getScriptProvider().getScriptContext(scriptName).getBindings("js").getMember(functionName);
                if (value.canExecute()) {
                    value.execute();
                }
            }
        }, delay, period);
        scriptTasks.put(scheduledTask, plugin.getScriptProvider().getScriptContext(scriptName));
        return scheduledTask;
    }

    @HostAccess.Export
    public ScheduledTask submitAsync(String scriptName, String functionName, long delay, long period) {
        ScheduledTask scheduledTask = plugin.submitAsync(() -> {
            if (plugin.getScriptProvider().isScriptLoaded(scriptName)) {
                Value value = plugin.getScriptProvider().getScriptContext(scriptName).getBindings("js").getMember(functionName);
                if (value.canExecute()) {
                    value.execute();
                }
            }
        }, delay, period);
        scriptTasks.put(scheduledTask, plugin.getScriptProvider().getScriptContext(scriptName));
        return scheduledTask;
    }

    
    public void cancelAllTasks() {
        scriptTasks.keySet().forEach(ScheduledTask::cancel);
    }

    public void clear() {
        scriptTasks.clear();
    }

    public void clearUuidContext(String uuid) {
        List<ScheduledTask> toRemove = new ArrayList<>();
        for (Map.Entry<ScheduledTask, Context> entry : scriptTasks.entrySet()) {
            if (entry.getValue().getBindings("js").getMember("__uuid__").asString().equals(uuid)) {
                toRemove.add(entry.getKey());
            }
        }
        for (ScheduledTask task : toRemove) {
            task.cancel();
            scriptTasks.remove(task);
        }
    }
}
