package dev.voxellink;

import dev.voxellink.adapter.CommandSender;
import dev.voxellink.adapter.NeoLogger;
import dev.voxellink.adapter.RemoteExecutor;
import dev.voxellink.bot.BotProvider;
import dev.voxellink.command.CommandProvider;
import dev.voxellink.config.ConfigProvider;
import dev.voxellink.game.GameEventListener;
import dev.voxellink.game.GameProvider;
import dev.voxellink.scheduler.SchedulerProvider;
import dev.voxellink.script.ScriptProvider;
import dev.voxellink.script.ScriptScheduler;
import dev.voxellink.storage.StorageProvider;
import dev.voxellink.discord.DiscordService;
import dev.voxellink.update.UpdateService;
import org.graalvm.polyglot.HostAccess;

import java.io.File;

public interface VoxelLink extends ConfigProvider, GameProvider, SchedulerProvider {
    default void enable() {
        try {
            getNeoLogger().info("Loading config...");
            loadConfig(this);
            getNeoLogger().info("Loading storage...");
            setStorageProvider(new StorageProvider(this));
            getStorageProvider().loadStorage();
            dev.voxellink.migrate.TableRenameMigration.migrate(this);
            getNeoLogger().info("Loading game events...");
            setGameEventListener(new GameEventListener(this));
            getNeoLogger().info("Registering commands...");
            registerCommands();
            getNeoLogger().info("Loading bot...");
            BotProvider botProvider = new BotProvider(this);
            setBotProvider(botProvider);
            getBotProvider().loadBot(this);
            getNeoLogger().info("Scheduling update checks...");
            UpdateService updateService = new UpdateService(this);
            setUpdateService(updateService);
            long checkIntervalSeconds = Math.max(1, getGeneralConfig().getInt("repository.auto-update.check-interval-minutes")) * 60L;
            submitAsync(updateService::checkForUpdate, 30, checkIntervalSeconds);
            getNeoLogger().info("Loading script system...");
            submitAsync(() -> {
                try {
                    setScriptScheduler(new ScriptScheduler(this));
                    ScriptProvider scriptProvider = new ScriptProvider(this);
                    setScriptProvider(scriptProvider);
                    scriptProvider.setBusinessActionExecutor(new dev.voxellink.script.VoxelLinkBusinessActionExecutor(this));
                    getScriptProvider().loadScript(this);
                    getGameEventListener().onPluginEnable();
                    if (!getBotProvider().awaitConnections(8000)) {
                        getNeoLogger().warn("Bot connections not fully established after 8s; "
                                + "server-start announcement may not reach every platform");
                    }
                    getDiscordService().notifyServerStarted();
                } catch (Throwable e) {
                    getNeoLogger().error("Failed to load script system", e);
                }
            });
        } catch (Throwable e) {
            getNeoLogger().error("Failed to load the plugin", e);
        }
    }
    
    default void disable() {
        // Bukkit calls disable even after a failed enable. Each subsystem is
        // therefore optional until its initialization has completed.
        if (getGameEventListener() != null) getGameEventListener().onPluginDisable();
        if (getBotProvider() != null) getDiscordService().notifyServerStopping();
        if (getGeneralConfig() != null) getGeneralConfig().flush(this);
        if (getMessageConfig() != null) getMessageConfig().flush(this);
        if (getScriptConfig() != null) getScriptConfig().flush(this);
        getNeoLogger().info("Unloading all scripts...");
        if (getScriptProvider() != null) getScriptProvider().unloadScript();
        if (getBotProvider() != null) getBotProvider().resetListeners();
        if (getGameEventListener() != null) getGameEventListener().reset();
        getNeoLogger().info("Cancelling all the tasks...");
        cancelAllTasks();
        if (getScriptScheduler() != null) getScriptScheduler().clear();
        getNeoLogger().info("Disconnecting to the bot...");
        if (getBotProvider() != null) getBotProvider().unloadBot();
        getNeoLogger().info("Saving data...");
        if (getStorageProvider() != null) getStorageProvider().closeStorage();
    }

    default void reload(CommandSender sender) {
        getGameEventListener().onPrePluginReload();
        getScriptProvider().setScriptSystemLoaded(false);
        cancelAllTasks();
        getScriptScheduler().clear();
        getNeoLogger().info("Reloading config...");
        try {
            reloadConfig(this);
        } catch (RuntimeException e) {
            String message = e.getMessage() == null ? e.toString() : e.getMessage();
            getNeoLogger().error("Config reload aborted; old configuration remains active", e);
            if (sender != null) sender.sendMessage("&c配置文件 JSON 无效，已保留旧配置: " + message);
            return;
        }
        submitAsync(() -> {
            try {
                getNeoLogger().info("Reloading bot...");
                getBotProvider().reloadBot(this);
                getBotProvider().resetListeners();
                getGameEventListener().reset();
                getNeoLogger().info("Reloading scripts...");
                getScriptProvider().unloadScript();
                getScriptProvider().loadScript(this);
                getGameEventListener().onPluginReloaded();
                getNeoLogger().info("Script system reloaded successfully");
            } catch (Throwable e) {
                getNeoLogger().error("Failed to reload scripts", e);
            }
            if (sender != null) {
                sender.sendMessage(getMessageConfig().getMessage("internal.reload.reloaded"));
            }
        });
    }

    @HostAccess.Export
    NeoLogger getNeoLogger();

    File getDataFolder();

    void setGameEventListener(GameEventListener listener);

    GameEventListener getGameEventListener();

    BotProvider getBotProvider();

    default DiscordService getDiscordService() { return getBotProvider().getDiscordService(); }

    void setBotProvider(BotProvider botProvider);

    UpdateService getUpdateService();

    void setUpdateService(UpdateService updateService);

    ScriptProvider getScriptProvider();

    void setScriptProvider(ScriptProvider scriptProvider);

    @HostAccess.Export
    ScriptScheduler getScriptScheduler();

    void setScriptScheduler(ScriptScheduler scriptScheduler);

    @HostAccess.Export
    StorageProvider getStorageProvider();

    void setStorageProvider(StorageProvider storageProvider);

    CommandProvider getCommandProvider();

    void setCommandProvider(CommandProvider commandProvider);

    void registerCommands();

    @HostAccess.Export
    String getPlatform();

    boolean isPluginLoaded(String name);

    @HostAccess.Export
    RemoteExecutor getExecutorByName(String name);

    String getVersion();

    boolean getOnlineMode();

    String getMinecraftVersion();

    String getBrand();

    /** Where a downloaded update jar for the given version should be staged so the host platform picks
     *  it up on next restart. Returns null on platforms that don't support staged updates, in which case
     *  the auto-update download step is skipped (the new-version notice still fires normally). */
    default File getUpdateStagingFile(String version) {
        return null;
    }
}
