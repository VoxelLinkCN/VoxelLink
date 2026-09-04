package dev.voxellink.adapter;

import org.graalvm.polyglot.HostAccess;

public class BukkitPlayer extends Player {
    private final org.bukkit.entity.Player player;

    public BukkitPlayer(org.bukkit.entity.Player player) {
        super(player.getName(), player.getUniqueId());
        this.player = player;
    }

    @HostAccess.Export
    @Override
    public void sendMessage(String message) {
        player.sendMessage(message);
    }

    @HostAccess.Export
    @Override
    public void kick(String message) {
        player.kickPlayer(message);
    }

    @HostAccess.Export
    @Override
    public boolean hasPermission(String node) {
        return player.hasPermission(node);
    }
}
