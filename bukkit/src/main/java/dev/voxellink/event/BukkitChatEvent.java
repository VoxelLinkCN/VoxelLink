package dev.voxellink.event;

import dev.voxellink.adapter.BukkitPlayer;
import dev.voxellink.game.event.ChatEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.graalvm.polyglot.HostAccess;

public class BukkitChatEvent extends ChatEvent {

    private final AsyncPlayerChatEvent event;

    public BukkitChatEvent(AsyncPlayerChatEvent event) {
        super(new BukkitPlayer(event.getPlayer()), event.getMessage());
        this.event = event;
    }

    @HostAccess.Export
    @Override
    public void disallow() {
        event.setCancelled(true);
    }
}
