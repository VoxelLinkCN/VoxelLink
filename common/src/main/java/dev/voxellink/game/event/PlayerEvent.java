package dev.voxellink.game.event;

import dev.voxellink.adapter.Player;
import lombok.Getter;
import org.graalvm.polyglot.HostAccess;

@Getter(onMethod_ = {@HostAccess.Export})
public class PlayerEvent {
    protected final Player player;

    public PlayerEvent(Player player) {
        this.player = player;
    }
}
