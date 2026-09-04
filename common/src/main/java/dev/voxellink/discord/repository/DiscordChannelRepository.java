package dev.voxellink.discord.repository;

import dev.voxellink.discord.model.DiscordChannelBinding;

import java.util.List;

public interface DiscordChannelRepository {
    void initialize();
    boolean add(DiscordChannelBinding binding);
    boolean remove(String guildId, String channelId);
    List<DiscordChannelBinding> findAll();
}
