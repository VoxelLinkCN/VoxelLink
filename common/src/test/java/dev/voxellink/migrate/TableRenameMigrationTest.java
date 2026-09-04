package dev.voxellink.migrate;

import dev.voxellink.adapter.NeoLogger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TableRenameMigrationTest {
    private Connection connection;
    private final List<String> infoMessages = new ArrayList<>();
    private final NeoLogger logger = new NeoLogger() {
        @Override public void info(String message) { infoMessages.add(message); }
        @Override public void warn(String message) { }
        @Override public void error(String message) { }
        @Override public void error(String message, Throwable throwable) { }
        @Override public void debug(String message) { }
        @Override public void trace(String message) { }
    };

    @BeforeEach
    void openDatabase(@TempDir Path tempDir) throws SQLException {
        File file = new File(tempDir.toFile(), "test.db");
        connection = DriverManager.getConnection("jdbc:sqlite:" + file.getAbsolutePath());
    }

    @AfterEach
    void closeDatabase() throws SQLException {
        connection.close();
    }

    @Test
    void renamesOldTableWhenNewTableIsAbsent() throws SQLException {
        createTable("neobot_repo");
        insertRow("neobot_repo", "https://example.com/repo.json");

        TableRenameMigration.migrate(connection, logger);

        assertFalse(TableRenameMigration.tableExists(connection, "neobot_repo"));
        assertTrue(TableRenameMigration.tableExists(connection, "voxellink_repo"));
        assertEquals("https://example.com/repo.json", selectUrl("voxellink_repo"));
        assertTrue(infoMessages.stream().anyMatch(m -> m.contains("neobot_repo") && m.contains("voxellink_repo")));
    }

    @Test
    void isANoOpWhenOldTableIsAbsent() throws SQLException {
        TableRenameMigration.migrate(connection, logger);

        assertFalse(TableRenameMigration.tableExists(connection, "neobot_metrics"));
        assertFalse(TableRenameMigration.tableExists(connection, "voxellink_metrics"));
        assertTrue(infoMessages.isEmpty());
    }

    @Test
    void isANoOpWhenNewTableAlreadyExists() throws SQLException {
        createTable("neobot_whitelist");
        insertRow("neobot_whitelist", "old-data");
        createTable("voxellink_whitelist");
        insertRow("voxellink_whitelist", "already-migrated-data");

        TableRenameMigration.migrate(connection, logger);

        assertTrue(TableRenameMigration.tableExists(connection, "neobot_whitelist"));
        assertEquals("already-migrated-data", selectUrl("voxellink_whitelist"));
        assertTrue(infoMessages.isEmpty());
    }

    @Test
    void runningTwiceInARowIsIdempotent() throws SQLException {
        createTable("neobot_discord_accounts");
        insertRow("neobot_discord_accounts", "value-a");

        TableRenameMigration.migrate(connection, logger);
        TableRenameMigration.migrate(connection, logger);

        assertTrue(TableRenameMigration.tableExists(connection, "voxellink_discord_accounts"));
        assertEquals("value-a", selectUrl("voxellink_discord_accounts"));
    }

    private void createTable(String name) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE " + name + " (url TEXT PRIMARY KEY)");
        }
    }

    private void insertRow(String table, String url) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute("INSERT INTO " + table + " (url) VALUES ('" + url + "')");
        }
    }

    private String selectUrl(String table) throws SQLException {
        try (Statement statement = connection.createStatement();
             java.sql.ResultSet rs = statement.executeQuery("SELECT url FROM " + table + " LIMIT 1")) {
            return rs.next() ? rs.getString("url") : null;
        }
    }
}
