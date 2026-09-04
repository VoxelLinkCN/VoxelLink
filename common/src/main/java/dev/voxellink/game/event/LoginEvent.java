package dev.voxellink.game.event;

import lombok.Getter;
import org.graalvm.polyglot.HostAccess;

import java.util.UUID;

@Getter(onMethod_ = {@HostAccess.Export})
public abstract class LoginEvent {
    private final String name;
    private final UUID uuid;

    protected LoginEvent(String name, UUID uuid) {
        this.name = name;
        this.uuid = uuid;
    }

    public abstract void disallow(String reason);
}
