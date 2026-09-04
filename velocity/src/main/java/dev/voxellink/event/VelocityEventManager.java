package dev.voxellink.event;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.event.player.PlayerChatEvent;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import dev.voxellink.VoxelLinkVelocity;
import dev.voxellink.adapter.VelocityPlayer;
import dev.voxellink.game.event.PlayerEvent;

public class VelocityEventManager {
    private final VoxelLinkVelocity plugin;

    public VelocityEventManager(VoxelLinkVelocity plugin) {
        this.plugin = plugin;
    }

    @Subscribe
    public void onLogin(LoginEvent loginEvent) {
        plugin.getGameEventListener().onLogin(new VelocityLoginEvent(loginEvent));
    }

    @Subscribe
    public void onJoin(ServerConnectedEvent event) {
        plugin.getGameEventListener().onJoin(new PlayerEvent(new VelocityPlayer(event.getPlayer())));
    }

    @Subscribe
    public void onQuit(DisconnectEvent event) {
        plugin.getGameEventListener().onQuit(new PlayerEvent(new VelocityPlayer(event.getPlayer())));
    }

    @Subscribe
    public void onChat(PlayerChatEvent event) {
        plugin.getGameEventListener().onChat(new VelocityChatEvent(event));
    }
}
