package dev.voxellink.adapter;

import lombok.Getter;
import org.graalvm.polyglot.HostAccess;


@Getter(onMethod_ = {@HostAccess.Export})
public abstract class CommandSender {
    private final String name;

    protected CommandSender(String name) {
        this.name = name;
    }

    @HostAccess.Export
    public abstract void sendMessage(String message);

    @HostAccess.Export
    public abstract boolean hasPermission(String node);
}
