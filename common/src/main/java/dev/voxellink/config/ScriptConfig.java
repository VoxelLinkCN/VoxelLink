package dev.voxellink.config;

import org.graalvm.polyglot.HostAccess;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.List;

public class ScriptConfig extends EnhancedConfig {
    private final EnhancedConfig generalConfig;

    public ScriptConfig(File file, JSONObject jsonObject, EnhancedConfig generalConfig) {
        super(file, jsonObject);
        this.generalConfig = generalConfig;
    }

    @HostAccess.Export
    @Override
    public String getString(String node) {
        try {
            return super.getString(node);
        } catch (Exception ignored) {
            return generalConfig.getString(node);
        }
    }

    @HostAccess.Export
    @Override
    public boolean getBoolean(String node) {
        try {
            return super.getBoolean(node);
        } catch (Exception ignored) {
            return generalConfig.getBoolean(node);
        }
    }

    @HostAccess.Export
    @Override
    public Config getObject(String node) {
        try {
            return super.getObject(node);
        } catch (Exception ignored) {
            return generalConfig.getObject(node);
        }
    }

    @HostAccess.Export
    @Override
    public List<String> getStringArray(String node) {
        try {
            return super.getStringArray(node);
        } catch (Exception ignored) {
            return generalConfig.getStringArray(node);
        }
    }

    @HostAccess.Export
    @Override
    public Config[] getArray(String node) {
        try {
            return super.getArray(node);
        } catch (Exception ignored) {
            return generalConfig.getArray(node);
        }
    }

    @HostAccess.Export
    @Override
    public double getDouble(String node) {
        try {
            return super.getDouble(node);
        } catch (Exception ignored) {
            return generalConfig.getDouble(node);
        }
    }

    @HostAccess.Export
    @Override
    public int getInt(String node) {
        try {
            return super.getInt(node);
        } catch (Exception ignored) {
            return generalConfig.getInt(node);
        }
    }

    @Override
    public JSONArray getJSONArray(String node) {
        try {
            return super.getJSONArray(node);
        } catch (Exception ignored) {
            return generalConfig.getJSONArray(node);
        }
    }

    @Override
    public JSONObject getJSONObject(String node) {
        try {
            return super.getJSONObject(node);
        } catch (Exception ignored) {
            return generalConfig.getJSONObject(node);
        }
    }

    @HostAccess.Export
    @Override
    public String[] getKeys() {
        try {
            return super.getKeys();
        } catch (Exception ignored) {
            return generalConfig.getKeys();
        }
    }

    @HostAccess.Export
    @Override
    public List<Long> getNumberArray(String node) {
        try {
            return super.getNumberArray(node);
        } catch (Exception ignored) {
            return generalConfig.getNumberArray(node);
        }
    }

    @HostAccess.Export
    @Override
    public String getMessage(String node) {
        try {
            return super.getMessage(node);
        } catch (Exception ignored) {
            return generalConfig.getMessage(node);
        }
    }
}
