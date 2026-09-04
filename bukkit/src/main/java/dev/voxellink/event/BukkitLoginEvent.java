package dev.voxellink.event;

import dev.voxellink.game.event.LoginEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.graalvm.polyglot.HostAccess;

public class BukkitLoginEvent extends LoginEvent {
    private final PlayerLoginEvent event;

    public BukkitLoginEvent(PlayerLoginEvent event) {
        super(event.getPlayer().getName(), event.getPlayer().getUniqueId());
        this.event = event;
    }

    @HostAccess.Export
    @Override
    public void disallow(String reason) {
        event.disallow(PlayerLoginEvent.Result.KICK_OTHER, reason);
    }
}
