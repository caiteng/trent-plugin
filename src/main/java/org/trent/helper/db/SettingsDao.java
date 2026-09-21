package org.trent.helper.db;

import com.intellij.openapi.diagnostic.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SettingsDao {
    private static final Logger LOG = Logger.getInstance(SettingsDao.class);

    public static String getConfig(String key, String defaultValue) {
        Connection conn = DatabaseManager.getInstance().getConnection();
        if (conn == null) return defaultValue;
        String sql = "SELECT value FROM plugin_config WHERE key=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, key);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String val = rs.getString("value");
                    return val != null ? val : defaultValue;
                }
            }
        } catch (SQLException e) {
            LOG.error("Failed to get config: " + key, e);
        }
        return defaultValue;
    }

    public static int getConfigInt(String key, int defaultValue) {
        String val = getConfig(key, null);
        if (val == null) return defaultValue;
        try {
            return Integer.parseInt(val);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public static void setConfig(String key, String value) {
        Connection conn = DatabaseManager.getInstance().getConnection();
        if (conn == null) return;
        String sql = "INSERT OR REPLACE INTO plugin_config (key, value) VALUES (?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, key);
            ps.setString(2, value);
            ps.executeUpdate();
        } catch (SQLException e) {
            LOG.error("Failed to set config: " + key, e);
        }
    }

    public static void setConfigInt(String key, int value) {
        setConfig(key, String.valueOf(value));
    }
}
