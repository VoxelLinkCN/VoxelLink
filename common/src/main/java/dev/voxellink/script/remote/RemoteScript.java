package dev.voxellink.script.remote;

import dev.voxellink.VoxelLink;
import dev.voxellink.util.HttpUtil;
import dev.voxellink.util.MapUtil;
import dev.voxellink.util.FileUtil;
import lombok.Data;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

@Data
public class RemoteScript {
    private final int schemaVersion;
    private final String type;
    private final String id;
    private final String name;
    private final String version;
    private final String author;
    private String description;
    private final String download;

    public String download(VoxelLink plugin) throws IOException {
        File tempFile = Files.createTempFile("voxellink", ".zip").toFile();
        HttpUtil.download(download, tempFile, MapUtil.of(), plugin.getGeneralConfig().getBoolean("repository.use-github-proxy"),
                plugin.getGeneralConfig().getString("repository.github-proxy-url"));
        File tempDir = Files.createTempDirectory("voxellink").toFile();
        FileUtil.unzip(tempFile, tempDir);
        if (tempDir.listFiles().length == 1) {
            tempDir = tempDir.listFiles()[0];
        }
        File scriptDir = new File(plugin.getDataFolder(), "scripts");
        if (type.equalsIgnoreCase("collection")) {
            FileUtil.copyFolder(tempDir, scriptDir);
            return "";
        } else {
            FileUtil.copyFolder(tempDir, new File(scriptDir, id));
            return id;
        }
    }
}
