package dev.voxellink.update;

import dev.voxellink.VoxelLink;
import dev.voxellink.util.HttpUtil;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;
import java.util.List;

/** Periodically checks GitHub for a newer VoxelLink release, notifies connected platforms, and (when
 *  repository.auto-update.enabled) stages the new jar for the next restart. Every failure path here
 *  degrades to a log message rather than propagating, since this runs on a background scheduler tick
 *  and must never take the plugin down. */
public class UpdateService {
    private final VoxelLink plugin;
    private final NewVersionNotifier notifier;

    /** Latest release info observed by the periodic check. The status command reads this instead of
     *  doing its own HTTP call: remote-command output capture only collects logs produced during the
     *  synchronous command dispatch, so any async output (the old per-call version check, which can
     *  take seconds through a proxy) was cut off after the first line. Null = not checked yet. */
    private volatile String cachedLatestVersion;
    private volatile String cachedLatestCommit;

    /** Version the platforms were last notified about. Without this guard every periodic check that
     *  still sees a newer release re-pings the QQ group owner / Discord channel over and over. */
    private volatile String lastNotifiedVersion;

    public UpdateService(VoxelLink plugin) {
        this(plugin, new NewVersionNotifier(plugin));
    }

    UpdateService(VoxelLink plugin, NewVersionNotifier notifier) {
        this.plugin = plugin;
        this.notifier = notifier;
    }

    public String cachedLatestVersion() { return cachedLatestVersion; }

    public String cachedLatestCommit() { return cachedLatestCommit; }

    public void checkForUpdate() {
        try {
            boolean needGithubProxy = plugin.getGeneralConfig().getBoolean("repository.use-github-proxy");
            String githubProxy = plugin.getGeneralConfig().getString("repository.github-proxy-url");
            String currentVersion = plugin.getVersion();
            JSONObject release = HttpUtil.getLatestReleaseJson(needGithubProxy, githubProxy);
            String latestVersion = release.getString("name");
            cachedLatestVersion = latestVersion;
            try {
                cachedLatestCommit = HttpUtil.getLatestCommit(needGithubProxy, githubProxy);
            } catch (Exception commitError) {
                // Version is already fresh; a failed commit lookup just keeps the previous value.
            }
            if (!isNewer(currentVersion, latestVersion)) return;
            if (latestVersion.equals(lastNotifiedVersion)) return;
            lastNotifiedVersion = latestVersion;
            plugin.getNeoLogger().info("A new VoxelLink version is available: " + latestVersion
                    + " (current: " + currentVersion + ")");
            notifier.notifyNewVersion(currentVersion, latestVersion);
            if (plugin.getGeneralConfig().getBoolean("repository.auto-update.enabled")) {
                downloadUpdate(release, latestVersion, needGithubProxy, githubProxy);
            }
        } catch (Exception error) {
            plugin.getNeoLogger().warn("Failed to check for updates: " + error.getMessage());
        }
    }

    private void downloadUpdate(JSONObject release, String latestVersion, boolean needGithubProxy, String legacyProxy) {
        try {
            String assetUrl = findJarAssetUrl(release);
            if (assetUrl == null) {
                plugin.getNeoLogger().warn("Auto-update enabled but release " + latestVersion + " has no downloadable .jar asset");
                return;
            }
            File staging = plugin.getUpdateStagingFile(latestVersion);
            if (staging == null) {
                plugin.getNeoLogger().warn("Auto-update enabled but this platform doesn't support staged updates");
                return;
            }
            int timeoutMs = plugin.getGeneralConfig().getInt("repository.auto-update.connect-timeout-ms");
            List<String> mirrors = plugin.getGeneralConfig().getStringArray("repository.auto-update.mirrors");
            String bestMirror = mirrors.isEmpty() ? null : HttpUtil.pickBestMirror(mirrors, timeoutMs);
            String proxyBase = bestMirror != null ? bestMirror : (needGithubProxy ? legacyProxy : null);
            File parent = staging.getParentFile();
            if (parent != null) parent.mkdirs();
            HttpUtil.download(assetUrl, staging, new HashMap<>(), proxyBase != null && !proxyBase.trim().isEmpty(), proxyBase);
            plugin.getNeoLogger().info("Downloaded VoxelLink " + latestVersion + " to " + staging.getPath()
                    + "; it will be applied on next restart");
        } catch (Exception error) {
            plugin.getNeoLogger().warn("Auto-update download failed, staying on current version: " + error.getMessage());
        }
    }

    static String findJarAssetUrl(JSONObject release) {
        if (!release.has("assets")) return null;
        JSONArray assets = release.getJSONArray("assets");
        for (int i = 0; i < assets.length(); i++) {
            JSONObject asset = assets.getJSONObject(i);
            if (asset.optString("name", "").endsWith(".jar")) {
                String url = asset.optString("browser_download_url", "");
                return url.isEmpty() ? null : url;
            }
        }
        return null;
    }

    /** Numeric dotted-version comparison (e.g. "1.4.2" vs "v1.10.0"); falls back to plain inequality
     *  for tags that don't parse as dotted numbers, so unrecognized formats still trigger a notice
     *  rather than being silently ignored. */
    static boolean isNewer(String current, String latest) {
        if (current == null || latest == null) return false;
        String normalizedCurrent = normalize(current);
        String normalizedLatest = normalize(latest);
        int[] currentParts = parseParts(normalizedCurrent);
        int[] latestParts = parseParts(normalizedLatest);
        if (currentParts == null || latestParts == null) return !normalizedCurrent.equals(normalizedLatest);
        int length = Math.max(currentParts.length, latestParts.length);
        for (int i = 0; i < length; i++) {
            int c = i < currentParts.length ? currentParts[i] : 0;
            int l = i < latestParts.length ? latestParts[i] : 0;
            if (l != c) return l > c;
        }
        return false;
    }

    private static String normalize(String version) {
        String trimmed = version.trim();
        return (trimmed.startsWith("v") || trimmed.startsWith("V")) ? trimmed.substring(1) : trimmed;
    }

    private static int[] parseParts(String version) {
        String[] segments = version.split("\\.");
        int[] parts = new int[segments.length];
        for (int i = 0; i < segments.length; i++) {
            if (!segments[i].matches("\\d+")) return null;
            parts[i] = Integer.parseInt(segments[i]);
        }
        return parts;
    }
}
