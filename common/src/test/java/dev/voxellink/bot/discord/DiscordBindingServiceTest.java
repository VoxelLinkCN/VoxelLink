package dev.voxellink.bot.discord;

import dev.voxellink.discord.DiscordService;
import dev.voxellink.discord.model.DiscordAccountBinding;
import dev.voxellink.discord.model.DiscordChannelBinding;
import dev.voxellink.discord.repository.DiscordAccountRepository;
import dev.voxellink.discord.repository.DiscordChannelRepository;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class DiscordBindingServiceTest {
    @Test
    void keepsSnowflakesAsStringsAndRejectsDuplicates() {
        MemoryChannels channels = new MemoryChannels();
        DiscordService service = new DiscordService(null, channels, new MemoryAccounts());
        assertTrue(service.bindChannel("9223372036854775807", "123456789012345678"));
        assertFalse(service.bindChannel("9223372036854775807", "123456789012345678"));
        assertFalse(service.bindChannel("not-a-snowflake", "123456789012345678"));
        assertEquals("123456789012345678", service.channels().get(0).getChannelId());
        assertTrue(service.unbindChannel("9223372036854775807", "123456789012345678"));
        assertFalse(service.unbindChannel("9223372036854775807", "123456789012345678"));
    }

    @Test
    void enforcesOnePlayerAndOneDiscordUserBindingBoundary() {
        MemoryAccounts accounts = new MemoryAccounts();
        DiscordService service = new DiscordService(null, new MemoryChannels(), accounts);
        UUID first = UUID.randomUUID();
        assertTrue(service.adminBind("first", first, "123456789012345678"));
        assertFalse(service.adminBind("first", first, "223456789012345678"));
        assertFalse(service.adminBind("second", UUID.randomUUID(), "123456789012345678"));
        assertTrue(service.unbindAccount(first));
        assertFalse(service.unbindAccount(first));
    }

    @Test
    void qqBindPersistsThePlayerNameForTheLoginGateFallback() {
        MemoryAccounts accounts = new MemoryAccounts();
        DiscordService service = new DiscordService(null, new MemoryChannels(), accounts);
        UUID bindTimeUuid = UUID.randomUUID();
        TestOfflinePlayer player = new TestOfflinePlayer("Steve", bindTimeUuid);

        assertTrue(service.bindFromQq("Steve", player, "1234567890"));
        assertEquals("Steve", accounts.findByMinecraftUuid(bindTimeUuid).getMinecraftName());
    }

    @Test
    void loginGateMatchesByNameWhenTheLoginUuidDiffersFromTheBindTimeUuid() {
        MemoryAccounts accounts = new MemoryAccounts();
        DiscordService service = new DiscordService(null, new MemoryChannels(), accounts);
        UUID bindTimeUuid = UUID.randomUUID();
        UUID loginUuid = UUID.randomUUID(); // e.g. Mojang uuid at login vs offline uuid stored at bind time
        TestOfflinePlayer player = new TestOfflinePlayer("Steve", bindTimeUuid);

        assertTrue(service.bindFromQq("Steve", player, "1234567890"));
        // Direct uuid lookup misses, so the plain gate without the name fallback would kick a bound player:
        assertNull(accounts.findByMinecraftUuid(loginUuid));
        // ...but the gate matches through the stored name:
        assertTrue(service.isAccountBound(loginUuid, "Steve"));
        assertTrue(service.isAccountBound(loginUuid, "steve")); // case-insensitive
        assertFalse(service.isAccountBound(UUID.randomUUID(), "Alex")); // never bound
        assertFalse(service.isAccountBound(loginUuid, null));
        assertFalse(service.isAccountBound(loginUuid, ""));
    }

    @Test
    void loginGateStillMatchesDirectlyByUuidWhenAvailable() {
        MemoryAccounts accounts = new MemoryAccounts();
        DiscordService service = new DiscordService(null, new MemoryChannels(), accounts);
        UUID bindTimeUuid = UUID.randomUUID();
        TestOfflinePlayer player = new TestOfflinePlayer("Steve", bindTimeUuid);

        assertTrue(service.bindFromQq("Steve", player, "1234567890"));
        assertTrue(service.isAccountBound(bindTimeUuid, null));
        assertTrue(service.isAccountBound(bindTimeUuid, "SomeoneElse"));
    }

    @Test
    void directBindingRejectsDuplicatesAndOnlyTheOwnerCanUnbind() {
        MemoryAccounts accounts = new MemoryAccounts();
        DiscordService service = new DiscordService(null, new MemoryChannels(), accounts);
        UUID playerId = UUID.randomUUID();
        TestOfflinePlayer player = new TestOfflinePlayer("Steve", playerId);

        assertEquals(DiscordService.AccountResult.SUCCESS,
                service.bindFromDiscord("Steve", player, "123456789012345678"));
        assertEquals(DiscordService.AccountResult.PLAYER_ALREADY_BOUND,
                service.bindFromDiscord("Steve", player, "223456789012345678"));
        assertEquals(DiscordService.AccountResult.NOT_OWNER,
                service.unbindFromDiscord("Steve", player, "223456789012345678"));
        assertEquals(DiscordService.AccountResult.SUCCESS,
                service.unbindFromDiscord("Steve", player, "123456789012345678"));
        assertEquals(DiscordService.AccountResult.INVALID_PLAYER,
                service.bindFromDiscord("not a player", player, "123456789012345678"));
    }

    private static class TestOfflinePlayer extends dev.voxellink.adapter.OfflinePlayer {
        private TestOfflinePlayer(String name, UUID uuid) { super(name, uuid); }
        public boolean isOnline() { return false; }
    }

    private static class MemoryChannels implements DiscordChannelRepository {
        private final List<DiscordChannelBinding> values = new ArrayList<>();
        public void initialize() { }
        public boolean add(DiscordChannelBinding binding) { if (findAll().stream().anyMatch(v -> v.getChannelId().equals(binding.getChannelId()))) return false; values.add(binding); return true; }
        public boolean remove(String guild, String channel) { return values.removeIf(v -> v.getGuildId().equals(guild) && v.getChannelId().equals(channel)); }
        public List<DiscordChannelBinding> findAll() { return new ArrayList<>(values); }
    }
    private static class MemoryAccounts implements DiscordAccountRepository {
        private final Map<UUID, DiscordAccountBinding> values = new HashMap<>();
        public void initialize() { }
        public DiscordAccountBinding findByMinecraftUuid(UUID id) { return values.get(id); }
        public List<DiscordAccountBinding> findByDiscordUserId(String id) { List<DiscordAccountBinding> result = new ArrayList<>(); for (DiscordAccountBinding v : values.values()) if (v.getDiscordUserId() != null && v.getDiscordUserId().equals(id)) result.add(v); return result; }
        public List<DiscordAccountBinding> findAll() { return new ArrayList<>(values.values()); }
        public boolean add(DiscordAccountBinding binding, int max) { if (values.containsKey(binding.getMinecraftUuid()) || findByDiscordUserId(binding.getDiscordUserId()).size() >= max) return false; values.put(binding.getMinecraftUuid(), binding); return true; }
        public boolean removeByMinecraftUuid(UUID id) { return values.remove(id) != null; }
        public boolean bindQqUser(UUID uuid, String qqUserId) { return bindQqUser(uuid, null, qqUserId); }
        public boolean bindQqUser(UUID uuid, String playerName, String qqUserId) {
            if (qqUserId == null || !qqUserId.matches("[0-9]{1,20}") || findByQqUserId(qqUserId) != null) return false;
            DiscordAccountBinding existing = values.get(uuid);
            if (existing != null && existing.getQqUserId() != null && !existing.getQqUserId().isEmpty()) return false;
            String name = playerName == null ? "" : playerName.trim();
            if (existing == null) {
                DiscordAccountBinding created = new DiscordAccountBinding(uuid, name, "");
                created.setQqUserId(qqUserId);
                values.put(uuid, created);
            } else {
                String storedName = existing.getMinecraftName() == null || existing.getMinecraftName().trim().isEmpty() ? name : existing.getMinecraftName();
                DiscordAccountBinding updated = new DiscordAccountBinding(uuid, storedName, existing.getDiscordUserId());
                updated.setQqUserId(qqUserId);
                values.put(uuid, updated);
            }
            return true;
        }
        public DiscordAccountBinding findByMinecraftNameIgnoreCase(String name) {
            if (name == null || name.trim().isEmpty()) return null;
            for (DiscordAccountBinding v : values.values()) if (name.trim().equalsIgnoreCase(v.getMinecraftName())) return v;
            return null;
        }
    }
}
