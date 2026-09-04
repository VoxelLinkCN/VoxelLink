package dev.voxellink.adapter;

import org.graalvm.polyglot.HostAccess;

public class BukkitCommandSender extends CommandSender {
    private final org.bukkit.command.CommandSender sender;

    public BukkitCommandSender(org.bukkit.command.CommandSender sender) {
        super(sender.getName());
        this.sender = sender;
    }

    @HostAccess.Export
    @Override
    public void sendMessage(String message) {
        sender.sendMessage(message);
    }

    @HostAccess.Export
    @Override
    public boolean hasPermission(String node) {
        return sender.hasPermission(node);
    }
}
