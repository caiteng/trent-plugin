package org.trent.helper.db;

import com.intellij.openapi.diagnostic.Logger;
import org.trent.helper.readtip.common.DataSource;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.IntConsumer;

public class DataSourceDao {
    private static final Logger LOG = Logger.getInstance(DataSourceDao.class);
    private static final int CHUNK_LINE_SIZE = 50;

    // ==================== data_source CRUD ====================

    public static long insertDataSource(DataSource ds) {
        Connection conn = DatabaseManager.getInstance().getConnection();
        if (conn == null) return -1;
        boolean originalAutoCommit = true;
        long generatedId = -1;
        try {
            originalAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO data_source (title, type) VALUES (?, ?)", Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, nullSafe(ds.getTitle()));
                ps.setString(2, nullSafe(ds.getType()));
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) generatedId = rs.getLong(1);
                }
            }
            if (generatedId > 0) {
                if ("remote".equals(ds.getType())) {
                    try (PreparedStatement ps = conn.prepareStatement(
                            "INSERT INTO remote_source (source_id, base_url, this_url, next_url, previous_url) VALUES (?, ?, ?, ?, ?)")) {
                        ps.setLong(1, generatedId);
                        ps.setString(2, nullSafe(ds.getBaseURL()));
                        ps.setString(3, nullSafe(ds.getThisURL()));
                        ps.setString(4, nullSafe(ds.getNextURL()));
                        ps.setString(5, nullSafe(ds.getPreviousURL()));
                        ps.executeUpdate();
                    }
                } else {
                    try (PreparedStatement ps = conn.prepareStatement(
                            "INSERT INTO local_source (source_id, local_path, this_url) VALUES (?, ?, ?)")) {
                        ps.setLong(1, generatedId);
                        ps.setString(2, nullSafe(ds.getBaseURL() != null ? ds.getBaseURL() : ds.getLocalPath()));
                        ps.setString(3, nullSafe(ds.getThisURL()));
                        ps.executeUpdate();
                    }
                }
            }
            conn.commit();
        } catch (SQLException e) {
            LOG.error("Failed to insert data source", e);
            try { conn.rollback(); } catch (Exception ignored) {}
        } finally {
            try { conn.setAutoCommit(originalAutoCommit); } catch (Exception ignored) {}
        }
        return generatedId;
    }

    public static void updateDataSource(DataSource ds) {
        Connection conn = DatabaseManager.getInstance().getConnection();
        if (conn == null) return;
        boolean originalAutoCommit = true;
        try {
            originalAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE data_source SET type=? WHERE title=?")) {
                ps.setString(1, nullSafe(ds.getType()));
                ps.setString(2, nullSafe(ds.getTitle()));
                ps.executeUpdate();
            }
            int sourceId = getSourceIdByTitle(ds.getTitle());
            if (sourceId > 0) {
                if ("remote".equals(ds.getType())) {
                    try (PreparedStatement ps = conn.prepareStatement(
                            "INSERT OR REPLACE INTO remote_source (source_id, base_url, this_url, next_url, previous_url) VALUES (?, ?, ?, ?, ?)")) {
                        ps.setInt(1, sourceId);
                        ps.setString(2, nullSafe(ds.getBaseURL()));
                        ps.setString(3, nullSafe(ds.getThisURL()));
                        ps.setString(4, nullSafe(ds.getNextURL()));
                        ps.setString(5, nullSafe(ds.getPreviousURL()));
                        ps.executeUpdate();
                    }
                } else {
                    try (PreparedStatement ps = conn.prepareStatement(
                            "INSERT OR REPLACE INTO local_source (source_id, local_path, this_url) VALUES (?, ?, ?)")) {
                        ps.setInt(1, sourceId);
                        ps.setString(2, nullSafe(ds.getBaseURL() != null ? ds.getBaseURL() : ds.getLocalPath()));
                        ps.setString(3, nullSafe(ds.getThisURL()));
                        ps.executeUpdate();
                    }
                }
            }
            conn.commit();
        } catch (SQLException e) {
            LOG.error("Failed to update data source", e);
            try { conn.rollback(); } catch (Exception ignored) {}
        } finally {
            try { conn.setAutoCommit(originalAutoCommit); } catch (Exception ignored) {}
        }
    }

    public static void deleteDataSource(String title) {
        Connection conn = DatabaseManager.getInstance().getConnection();
        if (conn == null) return;
        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM data_source WHERE title=?")) {
            ps.setString(1, title);
            ps.executeUpdate();
        } catch (SQLException e) {
            LOG.error("Failed to delete data source: " + title, e);
        }
    }

    public static List<DataSource> getAllDataSources() {
        List<DataSource> list = new ArrayList<>();
        Connection conn = DatabaseManager.getInstance().getConnection();
        if (conn == null) {
            LOG.warn("Database connection is null, returning empty data source list");
            return list;
        }
        String sql = """
            SELECT d.*, r.progress, r.page_index,
                   rs.base_url, rs.this_url, rs.next_url, rs.previous_url,
                   ls.local_path, ls.this_url AS local_this_url
            FROM data_source d
            LEFT JOIN reading_progress r ON d.id = r.source_id
            LEFT JOIN remote_source rs ON d.id = rs.source_id
            LEFT JOIN local_source ls ON d.id = ls.source_id
            ORDER BY d.id
        """;
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapDataSource(rs));
            }
        } catch (SQLException e) {
            LOG.error("Failed to load data sources", e);
        }
        return list;
    }

    public static DataSource getDataSourceByTitle(String title) {
        Connection conn = DatabaseManager.getInstance().getConnection();
        if (conn == null) return null;
        String sql = """
            SELECT d.*, r.progress, r.page_index,
                   rs.base_url, rs.this_url, rs.next_url, rs.previous_url,
                   ls.local_path, ls.this_url AS local_this_url
            FROM data_source d
            LEFT JOIN reading_progress r ON d.id = r.source_id
            LEFT JOIN remote_source rs ON d.id = rs.source_id
            LEFT JOIN local_source ls ON d.id = ls.source_id
            WHERE d.title=?
        """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, title);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapDataSource(rs);
            }
        } catch (SQLException e) {
            LOG.error("Failed to get data source by title: " + title, e);
        }
        return null;
    }

    public static DataSource getDataSourceById(int id) {
        Connection conn = DatabaseManager.getInstance().getConnection();
        if (conn == null) return null;
        String sql = """
            SELECT d.*, r.progress, r.page_index,
                   rs.base_url, rs.this_url, rs.next_url, rs.previous_url,
                   ls.local_path, ls.this_url AS local_this_url
            FROM data_source d
            LEFT JOIN reading_progress r ON d.id = r.source_id
            LEFT JOIN remote_source rs ON d.id = rs.source_id
            LEFT JOIN local_source ls ON d.id = ls.source_id
            WHERE d.id=?
        """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapDataSource(rs);
            }
        } catch (SQLException e) {
            LOG.error("Failed to get data source by id", e);
        }
        return null;
    }

    // ==================== active_source ====================

    public static void setActiveSource(String title) {
        Connection conn = DatabaseManager.getInstance().getConnection();
        if (conn == null) return;
        try {
            conn.createStatement().execute("DELETE FROM active_source");
            try (PreparedStatement ps = conn.prepareStatement("INSERT INTO active_source (source_id) SELECT id FROM data_source WHERE title=?")) {
                ps.setString(1, title);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            LOG.error("Failed to set active source", e);
        }
    }

    public static DataSource getActiveSource() {
        Connection conn = DatabaseManager.getInstance().getConnection();
        if (conn == null) return null;
        String sql = """
            SELECT d.*, r.progress, r.page_index,
                   rs.base_url, rs.this_url, rs.next_url, rs.previous_url,
                   ls.local_path, ls.this_url AS local_this_url
            FROM active_source a
            JOIN data_source d ON a.source_id = d.id
            LEFT JOIN reading_progress r ON d.id = r.source_id
            LEFT JOIN remote_source rs ON d.id = rs.source_id
            LEFT JOIN local_source ls ON d.id = ls.source_id
        """;
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return mapDataSource(rs);
        } catch (SQLException e) {
            LOG.error("Failed to get active source", e);
        }
        return null;
    }

    public static int getSourceIdByTitle(String title) {
        Connection conn = DatabaseManager.getInstance().getConnection();
        if (conn == null) return -1;
        String sql = "SELECT id FROM data_source WHERE title=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, title);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("id");
            }
        } catch (SQLException e) {
            LOG.error("Failed to get source id for: " + title, e);
        }
        return -1;
    }

    // ==================== reading_progress ====================

    public static void saveProgress(String title, int chunkIndex, int pageIndex) {
        Connection conn = DatabaseManager.getInstance().getConnection();
        if (conn == null) return;
        String sql = """
            INSERT INTO reading_progress (source_id, progress, page_index, updated_at)
            SELECT id, ?, ?, CURRENT_TIMESTAMP FROM data_source WHERE title=?
            ON CONFLICT(source_id) DO UPDATE SET progress=?, page_index=?, updated_at=CURRENT_TIMESTAMP
        """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, chunkIndex);
            ps.setInt(2, pageIndex);
            ps.setString(3, title);
            ps.setInt(4, chunkIndex);
            ps.setInt(5, pageIndex);
            ps.executeUpdate();
        } catch (SQLException e) {
            LOG.error("Failed to save progress for: " + title, e);
        }
    }

    public static void saveProgress(String title, int pageIndex) {
        saveProgress(title, 0, pageIndex);
    }

    // ==================== text_chunk ====================

    public static void saveChunk(int sourceId, int chunkIndex, String content) {
        Connection conn = DatabaseManager.getInstance().getConnection();
        if (conn == null) return;
        String sql = "INSERT OR REPLACE INTO text_chunk (source_id, chunk_index, content) VALUES (?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, sourceId);
            ps.setInt(2, chunkIndex);
            ps.setString(3, content);
            ps.executeUpdate();
        } catch (SQLException e) {
            LOG.error("Failed to save chunk " + chunkIndex + " for source " + sourceId, e);
        }
    }

    public static String loadChunk(int sourceId, int chunkIndex) {
        Connection conn = DatabaseManager.getInstance().getConnection();
        if (conn == null) return null;
        String sql = "SELECT content FROM text_chunk WHERE source_id=? AND chunk_index=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, sourceId);
            ps.setInt(2, chunkIndex);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getString("content");
            }
        } catch (SQLException e) {
            LOG.error("Failed to load chunk " + chunkIndex + " for source " + sourceId, e);
        }
        return null;
    }

    public static int getTotalChunks(int sourceId) {
        Connection conn = DatabaseManager.getInstance().getConnection();
        if (conn == null) return 0;
        String sql = "SELECT COALESCE(MAX(chunk_index), -1) + 1 as total FROM text_chunk WHERE source_id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, sourceId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("total");
            }
        } catch (SQLException e) {
            LOG.error("Failed to get total chunks for source " + sourceId, e);
        }
        return 0;
    }

    public static void deleteChunks(int sourceId) {
        Connection conn = DatabaseManager.getInstance().getConnection();
        if (conn == null) return;
        String sql = "DELETE FROM text_chunk WHERE source_id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, sourceId);
            ps.executeUpdate();
        } catch (SQLException e) {
            LOG.error("Failed to delete chunks for source " + sourceId, e);
        }
    }

    public static int importTxtFile(int sourceId, String filePath) {
        return importTxtFile(sourceId, filePath, null);
    }

    public static int importTxtFile(int sourceId, String filePath, IntConsumer progressCallback) {
        Connection conn = DatabaseManager.getInstance().getConnection();
        if (conn == null) return 0;
        deleteChunks(sourceId);
        List<String> chunks = splitTxtToChunks(filePath, CHUNK_LINE_SIZE, progressCallback);
        try (PreparedStatement ps = conn
                .prepareStatement("INSERT OR REPLACE INTO text_chunk (source_id, chunk_index, content) VALUES (?, ?, ?)")) {
            conn.setAutoCommit(false);
            for (int i = 0; i < chunks.size(); i++) {
                ps.setInt(1, sourceId);
                ps.setInt(2, i);
                ps.setString(3, chunks.get(i));
                ps.addBatch();
            }
            ps.executeBatch();
            conn.setAutoCommit(true);
        } catch (SQLException e) {
            LOG.error("Failed to batch insert chunks for source " + sourceId, e);
            try { conn.setAutoCommit(true); } catch (Exception ignored) {}
        }
        return chunks.size();
    }

    // ==================== 工具方法 ====================

    public static List<String> splitTxtToChunks(String filePath, int chunkLineSize) {
        return splitTxtToChunks(filePath, chunkLineSize, null);
    }

    public static List<String> splitTxtToChunks(String filePath, int chunkLineSize, IntConsumer progressCallback) {
        List<String> chunks = new ArrayList<>();
        List<String> currentLines = new ArrayList<>();
        int linesProcessed = 0;
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String trimmed = line.trim();
                if (trimmed.isEmpty()) continue;
                if (isSeparatorLine(trimmed)) continue;
                currentLines.add(trimmed);
                linesProcessed++;
                if (progressCallback != null) {
                    progressCallback.accept(linesProcessed);
                }
                if (currentLines.size() >= chunkLineSize) {
                    chunks.add(String.join("\n", currentLines));
                    currentLines.clear();
                }
            }
            if (!currentLines.isEmpty()) {
                chunks.add(String.join("\n", currentLines));
            }
        } catch (IOException e) {
            LOG.error("Failed to read file: " + filePath, e);
        }
        return chunks;
    }

    private static boolean isSeparatorLine(String line) {
        if (line.isEmpty()) return false;
        for (int i = 0; i < line.length(); i++) {
            if (line.charAt(i) != '-') return false;
        }
        return true;
    }

    private static boolean isChapterLine(String line) {
        return line.matches("^第[零一二三四五六七八九十百千万\\d]+章.*");
    }

    private static DataSource mapDataSource(ResultSet rs) throws SQLException {
        DataSource ds = new DataSource();
        ds.setTitle(rs.getString("title"));
        ds.setType(rs.getString("type"));
        int progress = rs.getInt("progress");
        int pageIndex = rs.getInt("page_index");
        if ("local".equals(ds.getType())) {
            ds.setLocalPath(rs.getString("local_path"));
            ds.setBaseURL(rs.getString("local_path"));
            ds.setThisURL(rs.getString("local_this_url"));
            ds.setLocalChunkIndex(progress);
            ds.setIndex(pageIndex);
        } else {
            ds.setBaseURL(rs.getString("base_url"));
            ds.setThisURL(rs.getString("this_url"));
            ds.setNextURL(rs.getString("next_url"));
            ds.setPreviousURL(rs.getString("previous_url"));
            ds.setIndex(progress);
        }
        return ds;
    }

    private static String nullSafe(String value) {
        return value == null ? "" : value;
    }
}
