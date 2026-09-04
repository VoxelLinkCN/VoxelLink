package dev.voxellink.adapter;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.graalvm.polyglot.HostAccess;

public class VelocityPlayer extends Player {

    private final com.velocitypowered.api.proxy.Player player;

    public VelocityPlayer(com.velocitypowered.api.proxy.Player player) {
        super(player.getUsername(), player.getUniqueId());
        this.player = player;
    }

    @HostAccess.Export
    @Override
    public void sendMessage(String message) {
        player.sendMessage(LegacyComponentSerializer.legacySection().deserialize(message));
    }

    @HostAccess.Export
    @Override
    public void kick(String message) {
        player.disconnect(LegacyComponentSerializer.legacySection().deserialize(message));
    }

    @Override
    public boolean hasPermission(String node) {
        return player.hasPermission(node);
    }
}
