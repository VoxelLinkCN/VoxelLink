package dev.voxellink.adapter;

import lombok.Getter;
import org.graalvm.polyglot.HostAccess;

import java.util.UUID;

@Getter(onMethod_ = {@HostAccess.Export})
public abstract class Player {
    private final String name;
    private final UUID uuid;

    protected Player(String name, UUID uuid) {
        this.name = name;
        this.uuid = uuid;
    }

    @HostAccess.Export
    public abstract void sendMessage(String message);

    @HostAccess.Export
    public abstract void kick(String message);

    @HostAccess.Export
    public abstract boolean hasPermission(String node);
}
