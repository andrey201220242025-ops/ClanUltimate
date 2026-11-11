package com.example.clanultimate;

import java.io.File;
import java.sql.*;

public class Database {
    private final ClanUltimatePlugin plugin;
    private Connection connection;

    public Database(ClanUltimatePlugin plugin) { this.plugin = plugin; }

    public void connect() throws Exception {
        File folder = plugin.getDataFolder();
        if (!folder.exists()) folder.mkdirs();
        File db = new File(folder, "clans.db");
        Class.forName("org.sqlite.JDBC");
        connection = DriverManager.getConnection("jdbc:sqlite:" + db.getAbsolutePath());
        try (Statement st = connection.createStatement()) { st.execute("PRAGMA foreign_keys = ON;"); }
    }

    public void disconnect() {
        try { if (connection != null && !connection.isClosed()) connection.close(); } catch (SQLException ignored) {}
    }

    public Connection getConnection() { return connection; }

    public void createTables() throws SQLException {
        try (Statement st = connection.createStatement()) {
            st.executeUpdate("CREATE TABLE IF NOT EXISTS clans (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "name TEXT UNIQUE, tag TEXT UNIQUE, owner TEXT, " +
                    "balance REAL DEFAULT 0, level INTEGER DEFAULT 1, xp INTEGER DEFAULT 0, " +
                    "prefix_left TEXT DEFAULT '§7[', prefix_text TEXT DEFAULT '', prefix_right TEXT DEFAULT '§7]', " +
                    "chest_data TEXT DEFAULT '', home_world TEXT, home_x REAL, home_y REAL, home_z REAL)");
            st.executeUpdate("CREATE TABLE IF NOT EXISTS members (uuid TEXT PRIMARY KEY, clan_id INTEGER, role TEXT)");
            st.executeUpdate("CREATE TABLE IF NOT EXISTS invites (clan_id INTEGER, uuid TEXT)");
        }
    }
}
