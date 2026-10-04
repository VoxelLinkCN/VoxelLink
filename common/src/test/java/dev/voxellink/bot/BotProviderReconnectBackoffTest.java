package dev.voxellink.bot;

import dev.voxellink.api.discord.client.DiscordClient;
import dev.voxellink.api.discord.data.DiscordMessage;
import dev.voxellink.api.listener.VBotListener;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * The QQ reconnect loop retries every check-interval tick by default. During a QQ risk-control
 * outage that hammers Tencent's servers and risks making the flag worse, so failed attempts back
 * off (doubling, capped) instead of retrying every tick; a successful reconnect resets the streak.
 *
 * The Discord sweep shares the same backoff: without it a disconnected gateway client was
 * reconnected on every tick, colliding with the client's own internal async reconnects — two
 * concurrent reconnects on the same WebSocket client can leave two live gateway sessions behind,
 * and every Discord message is then delivered twice, permanently.
 */
class BotProviderReconnectBackoffTest {

    @Test
    void firstAttemptUsesBaseDelay() {
        assertEquals(5_000L, BotProvider.nextBackoffMillis(0));
    }

    @Test
    void delayDoublesWithEachFailedStreak() {
        assertEquals(10_000L, BotProvider.nextBackoffMillis(1));
        assertEquals(20_000L, BotProvider.nextBackoffMillis(2));
        assertEquals(40_000L, BotProvider.nextBackoffMillis(3));
    }

    @Test
    void delayIsCappedAtMaximum() {
        assertEquals(300_000L, BotProvider.nextBackoffMillis(10));
        assertEquals(300_000L, BotProvider.nextBackoffMillis(1000));
    }

    @Test
    void discordSweepReconnectsDisconnectedClientsAndSchedulesTheNextAttempt() {
        Map<String, BotProvider.ReconnectBackoff> backoffs = new HashMap<>();
        FakeDiscordClient disconnected = new FakeDiscordClient(false);
        List<Long> reconnects = new ArrayList<>();
        long now = 1000L;

        int requested = BotProvider.sweepDisconnectedDiscordClients(
                Collections.singletonList(disconnected), backoffs, "discord", now, c -> reconnects.add(now));

        assertEquals(1, requested);
        assertEquals(Collections.singletonList(1000L), reconnects);
        BotProvider.ReconnectBackoff backoff = backoffs.get("discord");
        assertEquals(6000L, backoff.nextAttemptAtMillis, "first attempt schedules now + base delay");
        assertEquals(1, backoff.attemptStreak);
    }

    @Test
    void discordSweepSkipsWhileInsideTheBackoffWindow() {
        Map<String, BotProvider.ReconnectBackoff> backoffs = new HashMap<>();
        FakeDiscordClient disconnected = new FakeDiscordClient(false);
        List<Long> reconnects = new ArrayList<>();
        long first = 1000L;
        long second = 4000L;

        BotProvider.sweepDisconnectedDiscordClients(
                Collections.singletonList(disconnected), backoffs, "discord", first, c -> reconnects.add(first));
        int requested = BotProvider.sweepDisconnectedDiscordClients(
                Collections.singletonList(disconnected), backoffs, "discord", second, c -> reconnects.add(second));

        assertEquals(0, requested, "within the window no reconnect may be requested");
        assertEquals(1, reconnects.size(), "the earlier attempt must be the only one");
    }

    @Test
    void discordSweepActsAgainOnceTheWindowHasPassedAndBacksOffFurther() {
        Map<String, BotProvider.ReconnectBackoff> backoffs = new HashMap<>();
        FakeDiscordClient disconnected = new FakeDiscordClient(false);
        List<Long> reconnects = new ArrayList<>();
        long first = 1000L;
        long second = 6000L;

        BotProvider.sweepDisconnectedDiscordClients(
                Collections.singletonList(disconnected), backoffs, "discord", first, c -> reconnects.add(first));
        BotProvider.sweepDisconnectedDiscordClients(
                Collections.singletonList(disconnected), backoffs, "discord", second, c -> reconnects.add(second));

        assertEquals(2, reconnects.size());
        assertEquals(16_000L, backoffs.get("discord").nextAttemptAtMillis, "second attempt doubles the delay");
        assertEquals(2, backoffs.get("discord").attemptStreak);
    }

    @Test
    void discordSweepResetsTheBackoffOnceTheClientIsConnectedAgain() {
        Map<String, BotProvider.ReconnectBackoff> backoffs = new HashMap<>();
        FakeDiscordClient connected = new FakeDiscordClient(true);
        List<Long> reconnects = new ArrayList<>();
        long now = 1000L;

        BotProvider.sweepDisconnectedDiscordClients(
                Collections.singletonList(connected), backoffs, "discord", now, c -> reconnects.add(now));

        assertEquals(0, reconnects.size(), "connected clients are never reconnected");
        assertFalse(backoffs.containsKey("discord"), "the streak resets once the client is connected");
    }

    private static final class FakeDiscordClient implements DiscordClient {
        private final boolean connected;

        FakeDiscordClient(boolean connected) { this.connected = connected; }

        @Override public void connect() { }
        @Override public void disconnect() { }
        @Override public void shutdown() { }
        @Override public void reconnect() { }
        @Override public boolean isConnected() { return connected; }
        @Override public void addListener(VBotListener listener) { }
        @Override public void removeListener(VBotListener listener) { }
        @Override public boolean hasListener(VBotListener listener) { return false; }
        @Override public CompletableFuture<DiscordMessage> sendMessage(long channelId, String message) {
            return new CompletableFuture<>();
        }
        @Override public CompletableFuture<DiscordMessage> editMessage(long channelId, long messageId, String message) {
            return new CompletableFuture<>();
        }
        @Override public CompletableFuture<Void> deleteMessage(long channelId, long messageId) {
            return new CompletableFuture<>();
        }
    }
}
