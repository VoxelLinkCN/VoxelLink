package dev.voxellink.bot.discord;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Discord delivers MESSAGE_CREATE once per gateway session. Seeing the same message ID twice means
 * the client ended up with more than one live session (reconnect race) or a replayed delivery; the
 * deduplicator makes inbound forwarding idempotent by dropping repeats by ID.
 */
class DiscordMessageDeduplicatorTest {

    @Test
    void marksTheFirstDeliveryAndDropsExactRepeats() {
        DiscordMessageDeduplicator dedup = new DiscordMessageDeduplicator();

        assertTrue(dedup.firstTime("111"));
        assertFalse(dedup.firstTime("111"), "the same message ID delivered twice must be a repeat");
        assertFalse(dedup.firstTime("111"), "repeats must keep being dropped, not re-admitted");
    }

    @Test
    void differentMessageIdsAreIndependent() {
        DiscordMessageDeduplicator dedup = new DiscordMessageDeduplicator();

        assertTrue(dedup.firstTime("111"));
        assertTrue(dedup.firstTime("222"));
        assertFalse(dedup.firstTime("111"));
        assertFalse(dedup.firstTime("222"));
    }

    @Test
    void nullAndBlankIdsPassThroughSoMalformedPayloadsNeverSwallowValidOnes() {
        DiscordMessageDeduplicator dedup = new DiscordMessageDeduplicator();

        assertTrue(dedup.firstTime(null));
        assertTrue(dedup.firstTime(""));
        assertTrue(dedup.firstTime(null), "pass-through IDs are never recorded, so they never collide");
        assertEquals(0, dedup.size());
    }

    @Test
    void evictsTheOldestIdOnceCapacityIsReachedSoTheTrackerStaysBounded() {
        DiscordMessageDeduplicator dedup = new DiscordMessageDeduplicator(2);

        assertTrue(dedup.firstTime("111"));
        assertTrue(dedup.firstTime("222"));
        assertTrue(dedup.firstTime("333"), "capacity must not block brand-new messages");
        assertEquals(2, dedup.size());

        assertFalse(dedup.firstTime("222"), "recent id survives eviction");
        assertTrue(dedup.firstTime("111"), "the oldest id was evicted, so a re-delivery is admitted again");
    }
}
