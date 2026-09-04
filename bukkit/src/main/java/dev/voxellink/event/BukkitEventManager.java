package dev.voxellink.event;

import dev.voxellink.VoxelLink;
import dev.voxellink.adapter.BukkitPlayer;
import dev.voxellink.game.event.DeathEvent;
import dev.voxellink.game.event.PlayerEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;

public class BukkitEventManager implements Listener {
    private final VoxelLink plugin;

    public BukkitEventManager(VoxelLink plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onLogin(PlayerLoginEvent event) {
        plugin.getGameEventListener().onLogin(new BukkitLoginEvent(event));
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        plugin.getGameEventListener().onQuit(new PlayerEvent(new BukkitPlayer(event.getPlayer())));
    }

    @EventHandler(ignoreCancelled = true)
    public void onChat(AsyncPlayerChatEvent event) {
        plugin.getGameEventListener().onChat(new BukkitChatEvent(event));
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        plugin.getGameEventListener().onDeath(new DeathEvent(new BukkitPlayer(event.getEntity()), event.getDeathMessage()));
    }

    @EventHandler(ignoreCancelled = true)
    public void onJoin(PlayerJoinEvent event) {
        plugin.getGameEventListener().onJoin(new PlayerEvent(new BukkitPlayer(event.getPlayer())));
    }
}
