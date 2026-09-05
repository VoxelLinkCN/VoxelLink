package dev.voxellink.game;

import dev.voxellink.adapter.OfflinePlayer;
import dev.voxellink.adapter.Player;
import org.graalvm.polyglot.HostAccess;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public interface GameProvider {
    @HostAccess.Export
    Player getOnlinePlayer(String name);

    @HostAccess.Export
    Player[] getOnlinePlayers();

    @HostAccess.Export
    OfflinePlayer getOfflinePlayer(String name);

    @HostAccess.Export
    void broadcast(String message);

    @HostAccess.Export
    String externalParsePlaceholder(String message, OfflinePlayer player); // it supports Player (online)

    @HostAccess.Export
    default String parsePlaceholder(String message, OfflinePlayer player) {
        // Placeholder key now uses the voxellink prefix; old hand-written literal placeholders
        // will no longer match.
        String regex = "%internal_voxellink_player_list_([^%]*)%";

        List<String> players = Arrays.stream(getOnlinePlayers()).map(Player::getName).collect(Collectors.toList());

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(message);

        StringBuffer result = new StringBuffer();
        while (matcher.find()) {
            String extracted = matcher.group(1);
            String processed = String.join(extracted, players);
            matcher.appendReplacement(result, processed);
        }
        matcher.appendTail(result);

        return externalParsePlaceholder(result.toString(), player);
    }

    default String parsePlaceholder(String message, String playerName) {
        return parsePlaceholder(message, getOfflinePlayer(playerName));
    }
}
