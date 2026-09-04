package dev.voxellink.adapter;

import dev.voxellink.VoxelLink;
import dev.voxellink.VoxelLinkBukkit;
import dev.voxellink.command.CommandProvider;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class BukkitCommandProvider extends CommandProvider implements CommandExecutor {
    private final VoxelLinkBukkit plugin;

    public BukkitCommandProvider(VoxelLinkBukkit plugin) {
        super(plugin);
        this.plugin = plugin;
    }

    @Override
    public void registerCommand() {
        plugin.getCommand("voxellink").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        this.onCommand(new BukkitCommandSender(commandSender), strings);
        return false;
    }
}
