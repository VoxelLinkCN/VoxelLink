package dev.voxellink.migrate;

import dev.voxellink.VoxelLink;
import dev.voxellink.adapter.NeoLogger;
import dev.voxellink.storage.DatabaseStorage;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/** One-time rename of tables created under the old "neobot_" prefix to "voxellink_", run once at
 *  startup before any repository/command class touches them. Idempotent: a pair is skipped if the
 *  old table is absent or the new table already exists, so repeated runs and fresh installs are
 *  both no-ops. "ALTER TABLE old RENAME TO new" is valid syntax on both MySQL and SQLite (>=3.25),
 *  so no per-backend branching is needed for the rename itself. */
public class TableRenameMigration {
    static final String[][] RENAMES = {
            {"neobot_repo", "voxellink_repo"},
            {"neobot_discord_accounts", "voxellink_discord_accounts"},
            {"neobot_discord_channels", "voxellink_discord_channels"},
            {"neobot_whitelist", "voxellink_whitelist"},
            {"neobot_metrics", "voxellink_metrics"},
    };

    public static void migrate(VoxelLink plugin) {
        DatabaseStorage storage = plugin.getStorageProvider().getStorage();
        try (Connection connection = storage.table(RENAMES[0][1]).getDataSource().getConnection()) {
            migrate(connection, plugin.getNeoLogger());
        } catch (SQLException e) {
            plugin.getNeoLogger().error("Failed to run neobot_* -> voxellink_* table rename migration", e);
        }
    }

    static void migrate(Connection connection, NeoLogger logger) throws SQLException {
        for (String[] rename : RENAMES) {
            renameIfNeeded(connection, logger, rename[0], rename[1]);
        }
    }

    private static void renameIfNeeded(Connection connection, NeoLogger logger, String oldName, String newName) throws SQLException {
        if (!tableExists(connection, oldName) || tableExists(connection, newName)) {
            return;
        }
        try (Statement statement = connection.createStatement()) {
            statement.execute("ALTER TABLE " + oldName + " RENAME TO " + newName);
        }
        logger.info("Migrated table " + oldName + " -> " + newName);
    }

    static boolean tableExists(Connection connection, String tableName) throws SQLException {
        DatabaseMetaData metaData = connection.getMetaData();
        try (ResultSet rs = metaData.getTables(null, null, "%", new String[]{"TABLE"})) {
            while (rs.next()) {
                if (tableName.equalsIgnoreCase(rs.getString("TABLE_NAME"))) {
                    return true;
                }
            }
        }
        return false;
    }
}
