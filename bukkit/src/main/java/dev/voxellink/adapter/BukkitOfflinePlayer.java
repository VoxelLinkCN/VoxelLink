package dev.voxellink.adapter;

import org.bukkit.Bukkit;
import org.graalvm.polyglot.HostAccess;

import java.util.UUID;

public class BukkitOfflinePlayer extends OfflinePlayer {
    private final org.bukkit.OfflinePlayer player;

    public BukkitOfflinePlayer(String name, UUID uuid) {
        super(name, uuid);
        this.player = Bukkit.getOfflinePlayer(uuid);
    }

    public BukkitOfflinePlayer(org.bukkit.OfflinePlayer player) {
        super(player.getName(), player.getUniqueId());
        this.player = player;
    }

    @HostAccess.Export
    @Override
    public boolean isOnline() {
        return player.isOnline();
    }
}
