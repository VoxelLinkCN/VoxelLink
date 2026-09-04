package dev.voxellink.script.remote;

import dev.voxellink.VoxelLink;
import dev.voxellink.util.HttpUtil;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.*;

@Data
@RequiredArgsConstructor
public class Repository {
    private int schemaVersion;
    private String name;
    private String author;
    private String description;
    private String website;
    private final String url;
    private List<RemoteScript> scripts = new ArrayList<>();
    private static final Map<String, String> headers = new HashMap<>();

    static {
        headers.put("Accept", "application/vnd.github+json");
        headers.put("X-GitHub-Api-Version", "2022-11-28");
    }

    public void fetch(boolean needGithubProxy) throws IOException, JSONException {
        String s = HttpUtil.get(url, headers, needGithubProxy);
        JSONObject jsonContent = new JSONObject(s);
        schemaVersion = jsonContent.getInt("schema_version");
        name = jsonContent.getString("name");
        author = jsonContent.getString("author");
        description = jsonContent.getString("description");
        website = jsonContent.getString("website");
        JSONArray plugins = jsonContent.getJSONArray("plugins");
        for (int i = 0; i < plugins.length(); i++) {
            JSONObject pluginObj = plugins.getJSONObject(i);
            int schemaVersion = pluginObj.getInt("schema_version");
            String id = pluginObj.getString("id");
            String name = pluginObj.getString("name");
            String author = pluginObj.getString("author");
            String version = pluginObj.getString("version");
            String type = pluginObj.getString("type");
            String download = pluginObj.getString("download");
            RemoteScript script = new RemoteScript(schemaVersion, type, id, name, version, author, download);
            if (pluginObj.has("description")) {
                script.setDescription(pluginObj.getString("description"));
            }
            scripts.add(script);
        }
    }
}
