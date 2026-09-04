package dev.voxellink.game;

import dev.voxellink.VoxelLink;
import dev.voxellink.game.event.ChatEvent;
import dev.voxellink.game.event.DeathEvent;
import dev.voxellink.game.event.LoginEvent;
import dev.voxellink.adapter.Player;
import dev.voxellink.game.event.PlayerEvent;
import org.graalvm.polyglot.HostAccess;
import org.graalvm.polyglot.Value;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

public class GameEventListener {

    private final VoxelLink plugin;

    private final Map<Value, String> map = new IdentityHashMap<>();

    public GameEventListener(VoxelLink plugin) {
        this.plugin = plugin;
    }

    public void reset() {
        map.clear();
    }

    @HostAccess.Export
    public void register(String eventName, Value method) {
        if (method.canExecute()) map.put(method, eventName);
    }

    public void clearUuidContext(String uuid) {
        List<Value> toRemove = new ArrayList<>();
        for (Map.Entry<Value, String> entry : map.entrySet()) {
            if (entry.getKey().getContext().getBindings("js").getMember("__uuid__").asString().equals(uuid)) {
                toRemove.add(entry.getKey());
            }
        }
        for (Value value : toRemove) {
            map.remove(value);
        }
    }

    private void fireEvent(String eventName, Object... args) {
        for (Map.Entry<Value, String> entry : map.entrySet()) {
            try {
                if (entry.getValue().equals(eventName)) {
                    entry.getKey().execute(args);
                }
            } catch (Exception e) {
                plugin.getNeoLogger().warn("There's an error while firing event " + eventName + ": " + e);
            }
        }
    }

    public void onLogin(LoginEvent event) {
        fireEvent("LoginEvent", event);
    }

    public void onJoin(PlayerEvent event) {
        fireEvent("JoinEvent", event);
        dispatchPlayerStatus("PlayerJoinEvent", event);
    }

    public void onQuit(PlayerEvent event) {
        fireEvent("QuitEvent", event);
        dispatchPlayerStatus("PlayerQuitEvent", event);
    }

    private void dispatchPlayerStatus(String eventName, PlayerEvent event) {
        if (plugin.getScriptProvider() == null) {
            plugin.getNeoLogger().warn(eventName + " rejected: script system is not loaded");
            return;
        }
        dev.voxellink.script.ScriptDispatchResult result = plugin.getScriptProvider().dispatchBusiness(eventName, event);
        plugin.getScriptProvider().executeBusinessActions(result);
    }

    public void onDeath(DeathEvent event) {
        fireEvent("DeathEvent", event);
        dispatchPlayerStatus("PlayerDeathEvent", event);
    }

    public void onChat(ChatEvent event) {
        fireEvent("ChatEvent", event);
        if (plugin.getScriptProvider() == null) {
            plugin.getNeoLogger().warn("MinecraftChatEvent rejected: script system is not loaded");
            return;
        }
        dev.voxellink.script.ScriptDispatchResult result = plugin.getScriptProvider().dispatchBusiness("MinecraftChatEvent", event);
        plugin.getScriptProvider().executeBusinessActions(result);
    }

    public void onPluginEnable() {
        fireEvent("PluginEnableEvent");
    }

    public void onPluginDisable() {
        fireEvent("PluginDisableEvent");
    }

    public void onPrePluginReload() {
        fireEvent("PrePluginReloadEvent");
    }

    public void onPluginReloaded() {
        fireEvent("PluginReloadedEvent");
    }
}
