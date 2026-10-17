package dev.voxellink.discord.repository;

import dev.voxellink.discord.model.DiscordAccountBinding;

import java.util.List;
import java.util.UUID;

public interface DiscordAccountRepository {
    void initialize();
    DiscordAccountBinding findByMinecraftUuid(UUID uuid);
    List<DiscordAccountBinding> findByDiscordUserId(String userId);
    List<DiscordAccountBinding> findAll();
    boolean add(DiscordAccountBinding binding, int maximumBindingsPerUser);
    boolean removeByMinecraftUuid(UUID uuid);
    default DiscordAccountBinding findByQqUserId(String userId) { return null; }
    default boolean bindQqUser(UUID uuid, String qqUserId) { return false; }
    /** Name-carrying variant: the login gate falls back to a case-insensitive name match when the
     *  bind-time UUID resolution disagrees with the login profile UUID (online-mode servers without
     *  Mojang API access, or case differences), so the name must actually be persisted. */
    default boolean bindQqUser(UUID uuid, String playerName, String qqUserId) { return bindQqUser(uuid, qqUserId); }
    default boolean unbindQqUser(UUID uuid, String qqUserId) { return false; }
    default DiscordAccountBinding findByMinecraftNameIgnoreCase(String name) { return null; }
}
