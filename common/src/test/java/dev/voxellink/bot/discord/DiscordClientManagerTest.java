package dev.voxellink.bot.discord;

import dev.voxellink.api.discord.client.DiscordClient;
import dev.voxellink.api.discord.data.DiscordMessage;
import dev.voxellink.api.listener.VBotListener;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;

class DiscordClientManagerTest {
    @Test
    void ownsConnectReconnectAndShutdownLifecycle() {
        DiscordClientManager manager = new DiscordClientManager();
        FakeClient client = new FakeClient();
        VBotListener listener = new VBotListener() {};

        manager.addAndConnect("discord", client, listener);
        assertEquals(1, client.connectCalls);
        assertSame(listener, client.listener);
        assertEquals(1, manager.getClients().size());

        client.connected = false;
        manager.reconnectDisconnected();
        assertEquals(1, client.reconnectCalls);

        manager.shutdownAll();
        assertEquals(1, client.shutdownCalls);
        assertTrue(manager.getClients().isEmpty());
    }

    @Test
    void shutsDownThePreviousClientWhenAddAndConnectIsCalledAgainForTheSameType() {
        DiscordClientManager manager = new DiscordClientManager();
        FakeClient first = new FakeClient();
        FakeClient second = new FakeClient();
        VBotListener listener = new VBotListener() {};

        manager.addAndConnect("discord", first, listener);
        manager.addAndConnect("discord", second, listener);

        // Regression: a stale first client left connected here kept receiving and re-dispatching every
        // gateway event forever, producing duplicate replies (e.g. one command answered 3x) with no way
        // for the orphaned connection to self-heal.
        assertEquals(1, first.shutdownCalls, "the superseded client must be shut down, not left connected");
        assertNull(first.listener, "the superseded client's listener must be detached so it stops dispatching");
        assertEquals(1, manager.getClients().size());
        assertSame(second, manager.getClients().iterator().next());
    }

    private static class FakeClient implements DiscordClient {
        private int connectCalls;
        private int reconnectCalls;
        private int shutdownCalls;
        private boolean connected;
        private VBotListener listener;

        @Override public void connect() { connectCalls++; connected = true; }
        @Override public void disconnect() { connected = false; }
        @Override public void shutdown() { shutdownCalls++; connected = false; }
        @Override public void reconnect() { reconnectCalls++; connected = true; }
        @Override public boolean isConnected() { return connected; }
        @Override public void addListener(VBotListener listener) { this.listener = listener; }
        @Override public void removeListener(VBotListener listener) { if (this.listener == listener) this.listener = null; }
        @Override public boolean hasListener(VBotListener listener) { return this.listener == listener; }
        @Override public CompletableFuture<DiscordMessage> sendMessage(long channelId, String content) { return new CompletableFuture<>(); }
        @Override public CompletableFuture<DiscordMessage> editMessage(long channelId, long messageId, String content) { return new CompletableFuture<>(); }
        @Override public CompletableFuture<Void> deleteMessage(long channelId, long messageId) { return new CompletableFuture<>(); }
    }
}
