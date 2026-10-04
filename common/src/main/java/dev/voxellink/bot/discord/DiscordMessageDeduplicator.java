package dev.voxellink.bot.discord;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/** Bounded first-seen tracker for Discord message IDs.
 *
 *  Discord delivers MESSAGE_CREATE exactly once per gateway session, so the same message ID
 *  arriving twice means the client ended up with more than one live session (a reconnect race
 *  on the underlying WebSocket client) or the delivery was replayed. Forwarding a repeat would
 *  duplicate the message in Minecraft and in any script-driven Discord sends. Dropping repeats
 *  by ID makes inbound forwarding idempotent no matter how many sessions the client has.
 *
 *  Bounded so a long-lived server cannot grow the set forever: once the capacity is reached the
 *  oldest tracked ID is evicted. A duplicate older than the newest {@code capacity} messages is
 *  not a realistic delivery race, so eviction is safe. */
final class DiscordMessageDeduplicator {
    private final Map<String, Boolean> seen = new LinkedHashMap<>();
    private final int capacity;

    DiscordMessageDeduplicator() { this(4096); }

    DiscordMessageDeduplicator(int capacity) { this.capacity = Math.max(1, capacity); }

    /** True when messageId was never seen before (and records it); false for repeats. */
    synchronized boolean firstTime(String messageId) {
        if (messageId == null || messageId.isEmpty()) return true;
        boolean first = seen.put(messageId, Boolean.TRUE) == null;
        while (seen.size() > capacity) {
            Iterator<String> eldest = seen.keySet().iterator();
            eldest.next();
            eldest.remove();
        }
        return first;
    }

    synchronized int size() { return seen.size(); }
}
