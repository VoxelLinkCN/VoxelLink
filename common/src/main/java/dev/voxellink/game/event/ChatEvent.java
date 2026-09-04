package dev.voxellink.game.event;

import dev.voxellink.adapter.Player;
import lombok.Getter;
import org.graalvm.polyglot.HostAccess;

@Getter(onMethod_ = {@HostAccess.Export})
public abstract class ChatEvent extends PlayerEvent {
    private final String message;

    protected ChatEvent(Player player, String message) {
        super(player);
        this.message = message;
    }

    @HostAccess.Export
    public abstract void disallow();
}
