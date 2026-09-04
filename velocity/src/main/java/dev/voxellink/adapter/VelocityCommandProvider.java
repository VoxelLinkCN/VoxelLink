package dev.voxellink.adapter;

import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.command.SimpleCommand;
import dev.voxellink.command.CommandProvider;
import dev.voxellink.VoxelLinkVelocity;

public class VelocityCommandProvider extends CommandProvider implements SimpleCommand {
    private final VoxelLinkVelocity plugin;

    public VelocityCommandProvider(VoxelLinkVelocity plugin) {
        super(plugin);
        this.plugin = plugin;
    }

    @Override
    public void execute(Invocation invocation) {
        this.onCommand(new VelocityCommandSender(invocation.source()), invocation.arguments());
    }

    @Override
    public void registerCommand() {
        CommandManager commandManager = plugin.getProxyServer().getCommandManager();
        CommandMeta commandMeta = commandManager.metaBuilder("voxellink").plugin(plugin).build();
        commandManager.register(commandMeta, this);
    }
}
