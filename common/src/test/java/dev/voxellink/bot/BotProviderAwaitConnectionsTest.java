package dev.voxellink.bot;

import dev.neovoxel.nbapi.action.Action;
import dev.neovoxel.nbapi.action.get.GetAction;
import dev.neovoxel.nbapi.client.NBotClient;
import dev.neovoxel.nbapi.discord.client.DiscordClient;
import dev.neovoxel.nbapi.discord.data.DiscordMessage;
import dev.neovoxel.nbapi.listener.NBotListener;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * QQ (OBWSClient) and Discord gateway connects are asynchronous: connect() returns before onOpen
 * fires. Sending the server-start announcement right after loadBot() therefore races the handshake
 * and fails on any link slower than an instant local one. allConnected() is what awaitConnections()
 * polls to decide whether to wait before firing that announcement.
 */
class BotProviderAwaitConnectionsTest {

    @Test
    void noConfiguredClientsIsTreatedAsConnected() {
        assertTrue(BotProvider.allConnected(Collections.emptyList(), Collections.emptyList()));
    }

    @Test
    void allConnectedClientsIsConnected() {
        assertTrue(BotProvider.allConnected(
                Arrays.asList(new FakeBotClient(true)),
                Arrays.asList(new FakeDiscordClient(true))));
    }

    @Test
    void oneDisconnectedQqClientIsNotConnected() {
        assertFalse(BotProvider.allConnected(
                Arrays.asList(new FakeBotClient(true), new FakeBotClient(false)),
                Collections.emptyList()));
    }

    @Test
    void oneDisconnectedDiscordClientIsNotConnected() {
        assertFalse(BotProvider.allConnected(
                Collections.emptyList(),
                Arrays.asList(new FakeDiscordClient(false))));
    }

    /**
     * Regression: BotProvider.addBot used to just overwrite the clients map, leaving a previous
     * client for the same type connected and still dispatching every inbound event forever (an
     * orphaned connection duplicating replies — same underlying bug class as
     * DiscordClientManagerTest's superseded-client case, on the QQ/NBotClient side instead of Discord).
     */
    @Test
    void closePreviousDisconnectsAndDetachesTheSupersededClient() {
        FakeBotClient previous = new FakeBotClient(true);
        FakeBotClient replacement = new FakeBotClient(true);
        NBotListener listener = new NBotListener() {};
        previous.addListener(listener);

        BotProvider.closePrevious(previous, replacement, listener);

        assertTrue(previous.disconnectCalled, "the superseded client must be disconnected, not left connected");
        assertFalse(previous.hasListener(listener), "the superseded client's listener must be detached so it stops dispatching");
    }

    @Test
    void closePreviousIsANoOpWhenThereWasNoPreviousClientOrItIsTheSameInstance() {
        FakeBotClient client = new FakeBotClient(true);
        NBotListener listener = new NBotListener() {};

        BotProvider.closePrevious(null, client, listener);
        BotProvider.closePrevious(client, client, listener);

        assertFalse(client.disconnectCalled, "must not disconnect a null previous or the client replacing itself");
    }

    private static class FakeBotClient implements NBotClient {
        private final boolean connected;
        private boolean disconnectCalled;
        private NBotListener listener;
        FakeBotClient(boolean connected) { this.connected = connected; }
        @Override public void connect() {}
        @Override public void disconnect() { disconnectCalled = true; }
        @Override public void reconnect() {}
        @Override public void addListener(NBotListener listener) { this.listener = listener; }
        @Override public void removeListener(NBotListener listener) { if (this.listener == listener) this.listener = null; }
        @Override public boolean hasListener(NBotListener listener) { return this.listener == listener; }
        @Override public void action(Action action) {}
        @Override public <T> void action(GetAction<T> action, Consumer<T> consumer) {}
        @Override public boolean isConnected() { return connected; }
    }

    private static class FakeDiscordClient implements DiscordClient {
        private final boolean connected;
        FakeDiscordClient(boolean connected) { this.connected = connected; }
        @Override public void connect() {}
        @Override public void disconnect() {}
        @Override public void shutdown() {}
        @Override public void reconnect() {}
        @Override public boolean isConnected() { return connected; }
        @Override public void addListener(NBotListener listener) {}
        @Override public void removeListener(NBotListener listener) {}
        @Override public boolean hasListener(NBotListener listener) { return false; }
        @Override public CompletableFuture<DiscordMessage> sendMessage(long channelId, String content) { return new CompletableFuture<>(); }
        @Override public CompletableFuture<DiscordMessage> editMessage(long channelId, long messageId, String content) { return new CompletableFuture<>(); }
        @Override public CompletableFuture<Void> deleteMessage(long channelId, long messageId) { return new CompletableFuture<>(); }
    }
}
