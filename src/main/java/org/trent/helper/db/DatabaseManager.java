package org.trent.helper.db;

import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.diagnostic.Logger;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;

public class DatabaseManager {
    private static final Logger LOG = Logger.getInstance(DatabaseManager.class);
    private static DatabaseManager instance;
    private Connection connection;
    private String databasePath;

    private DatabaseManager() {
    }

    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    public synchronized Connection getConnection() {
        if (connection == null) {
            String path = (databasePath != null && !databasePath.isEmpty()) ? databasePath : getDefaultPath();
            initDatabase(path);
        }
        if (connection == null) {
            // 重试一次
            String path = (databasePath != null && !databasePath.isEmpty()) ? databasePath : getDefaultPath();
            initDatabase(path);
        }
        return connection;
    }

    public synchronized void initDatabase(String path) {
        close();
        this.databasePath = path;
        try {
            if (path != null && !path.isEmpty()) {
                new File(path).getParentFile().mkdirs();
            }
            Class.forName("org.sqlite.JDBC", true, DatabaseManager.class.getClassLoader());
            connection = DriverManager.getConnection("jdbc:sqlite:" + path);
            initSchema();
            LOG.info("SQLite database connected: " + path);
        } catch (Exception e) {
            LOG.error("Failed to initialize SQLite database: " + path, e);
        }
    }

    public synchronized void reconnect(String newPath) {
        initDatabase(newPath);
    }

    public synchronized String getDatabasePath() {
        return databasePath;
    }

    private String getDefaultPath() {
        String dbDir = PathManager.getPluginsDir() + File.separator + "trent-helper";
        new File(dbDir).mkdirs();
        return dbDir + File.separator + "trent.db";
    }

    private void initSchema() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate("PRAGMA foreign_keys = ON");
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS data_source (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    title TEXT UNIQUE NOT NULL,
                    type TEXT DEFAULT 'remote',
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
            """);
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS remote_source (
                    source_id INTEGER PRIMARY KEY REFERENCES data_source(id) ON DELETE CASCADE,
                    base_url TEXT DEFAULT '',
                    this_url TEXT DEFAULT '',
                    next_url TEXT DEFAULT '',
                    previous_url TEXT DEFAULT ''
                )
            """);
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS local_source (
                    source_id INTEGER PRIMARY KEY REFERENCES data_source(id) ON DELETE CASCADE,
                    local_path TEXT DEFAULT '',
                    this_url TEXT DEFAULT ''
                )
            """);
            // 兼容旧库：若 local_source 缺少 this_url 列则补充
            try (ResultSet rs = stmt.executeQuery("PRAGMA table_info(local_source)")) {
                boolean hasThisUrl = false;
                while (rs.next()) {
                    if ("this_url".equals(rs.getString("name"))) { hasThisUrl = true; break; }
                }
                if (!hasThisUrl) {
                    stmt.executeUpdate("ALTER TABLE local_source ADD COLUMN this_url TEXT DEFAULT ''");
                }
            }
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS text_chunk (
                    source_id INTEGER NOT NULL REFERENCES data_source(id) ON DELETE CASCADE,
                    chunk_index INTEGER NOT NULL,
                    content TEXT,
                    PRIMARY KEY (source_id, chunk_index)
                )
            """);
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS reading_progress (
                    source_id INTEGER PRIMARY KEY REFERENCES data_source(id) ON DELETE CASCADE,
                    progress INTEGER DEFAULT 0,
                    page_index INTEGER DEFAULT 0,
                    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
            """);
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS active_source (
                    source_id INTEGER REFERENCES data_source(id),
                    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
            """);
        }
    }

    /**
     * 检查数据库表结构是否与最新 schema 一致
     */
    public synchronized boolean checkSchema() {
        if (connection == null) return false;
        try (Statement stmt = connection.createStatement()) {
            Set<String> tables = new HashSet<>();
            try (ResultSet rs = stmt.executeQuery(
                    "SELECT name FROM sqlite_master WHERE type='table' AND name IN ('data_source','remote_source','local_source','text_chunk','reading_progress','active_source')")) {
                while (rs.next()) tables.add(rs.getString("name"));
            }
            if (tables.size() != 6) return false;
            Set<String> rpColumns = new HashSet<>();
            try (ResultSet rs = stmt.executeQuery("PRAGMA table_info(reading_progress)")) {
                while (rs.next()) rpColumns.add(rs.getString("name"));
            }
            return rpColumns.contains("progress") && rpColumns.contains("page_index");
        } catch (SQLException e) {
            LOG.error("Failed to check schema", e);
            return false;
        }
    }

    /**
     * 删除所有表并按最新 schema 重建
     */
    public synchronized void rebuildSchema() {
        if (connection == null) return;
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate("PRAGMA foreign_keys = OFF");
            stmt.executeUpdate("DROP TABLE IF EXISTS active_source");
            stmt.executeUpdate("DROP TABLE IF EXISTS reading_progress");
            stmt.executeUpdate("DROP TABLE IF EXISTS text_chunk");
            stmt.executeUpdate("DROP TABLE IF EXISTS local_source");
            stmt.executeUpdate("DROP TABLE IF EXISTS remote_source");
            stmt.executeUpdate("DROP TABLE IF EXISTS data_source");
            stmt.executeUpdate("PRAGMA foreign_keys = ON");
            initSchema();
            LOG.info("Database schema rebuilt: " + databasePath);
        } catch (SQLException e) {
            LOG.error("Failed to rebuild schema", e);
        }
    }

    public synchronized void close() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                LOG.error("Failed to close database connection", e);
            }
            connection = null;
        }
    }
}
