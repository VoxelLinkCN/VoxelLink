package dev.voxellink.game.event;

import dev.voxellink.adapter.Player;
import lombok.Getter;
import org.graalvm.polyglot.HostAccess;

@Getter(onMethod_ = {@HostAccess.Export})
public class DeathEvent extends PlayerEvent {
    private final String message;

    public DeathEvent(Player player, String deathMessage) {
        super(player);
        this.message = deathMessage;
    }
}
