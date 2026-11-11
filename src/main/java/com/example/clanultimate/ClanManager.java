package com.example.clanultimate;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.sql.*;
import java.util.*;

public class ClanManager {
    private final ClanUltimatePlugin plugin;
    private final Database db;
    private final Map<UUID, Integer> playerClan = new HashMap<>();
    private final Map<UUID, String> playerRole = new HashMap<>();
    private final Map<Integer, String> clanTags = new HashMap<>();
    private final Map<Integer, String> clanNames = new HashMap<>();

    public ClanManager(ClanUltimatePlugin plugin, Database db) {
        this.plugin = plugin;
        this.db = db;
        loadCache();
    }

    public void loadCache() {
        playerClan.clear(); playerRole.clear(); clanTags.clear(); clanNames.clear();
        try (Statement st = db.getConnection().createStatement()) {
            ResultSet rs = st.executeQuery("SELECT id, name, tag FROM clans");
            while (rs.next()) {
                clanNames.put(rs.getInt("id"), rs.getString("name"));
                clanTags.put(rs.getInt("id"), rs.getString("tag"));
            }
            rs = st.executeQuery("SELECT uuid, clan_id, role FROM members");
            while (rs.next()) {
                UUID u = UUID.fromString(rs.getString("uuid"));
                playerClan.put(u, rs.getInt("clan_id"));
                playerRole.put(u, rs.getString("role"));
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public String createClan(Player p, String name, String tag) {
        if (playerClan.containsKey(p.getUniqueId())) return "§cВы уже в клане.";
        try {
            PreparedStatement ps = db.getConnection().prepareStatement("INSERT INTO clans (name, tag, owner) VALUES (?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, name); ps.setString(2, tag); ps.setString(3, p.getUniqueId().toString());
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                int id = rs.getInt(1);
                addMember(id, p.getUniqueId(), "OWNER");
                loadCache();
                return "§aКлан §e" + name + " §aсоздан!";
            }
        } catch (SQLException e) { return "§cИмя или тег заняты."; }
        return "§cОшибка.";
    }

    public void addMember(int clanId, UUID uuid, String role) throws SQLException {
        PreparedStatement ps = db.getConnection().prepareStatement("INSERT INTO members (uuid, clan_id, role) VALUES (?, ?, ?)");
        ps.setString(1, uuid.toString()); ps.setInt(2, clanId); ps.setString(3, role);
        ps.executeUpdate();
    }

    public void invite(Player target, int clanId) {
        try {
            PreparedStatement ps = db.getConnection().prepareStatement("INSERT INTO invites (clan_id, uuid) VALUES (?, ?)");
            ps.setInt(1, clanId); ps.setString(2, target.getUniqueId().toString());
            ps.executeUpdate();
        } catch (SQLException ignored) {}
    }

    public String acceptInvite(Player p) {
        if (playerClan.containsKey(p.getUniqueId())) return "§cВы уже в клане.";
        try {
            PreparedStatement ps = db.getConnection().prepareStatement("SELECT clan_id FROM invites WHERE uuid = ?");
            ps.setString(1, p.getUniqueId().toString());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int id = rs.getInt("clan_id");
                addMember(id, p.getUniqueId(), "MEMBER");
                db.getConnection().prepareStatement("DELETE FROM invites WHERE uuid = '" + p.getUniqueId() + "'").executeUpdate();
                loadCache();
                return "§aВы вступили в клан §e" + clanNames.get(id);
            }
        } catch (SQLException ignored) {}
        return "§cНет приглашений.";
    }

    public double getBalance(int clanId) {
        try (PreparedStatement ps = db.getConnection().prepareStatement("SELECT balance FROM clans WHERE id = ?")) {
            ps.setInt(1, clanId); ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getDouble("balance");
        } catch (SQLException ignored) {} return 0;
    }

    public void setBalance(int clanId, double amount) {
        try (PreparedStatement ps = db.getConnection().prepareStatement("UPDATE clans SET balance = ? WHERE id = ?")) {
            ps.setDouble(1, amount); ps.setInt(2, clanId); ps.executeUpdate();
        } catch (SQLException ignored) {}
    }

    public int getLevel(int clanId) {
        try (PreparedStatement ps = db.getConnection().prepareStatement("SELECT level FROM clans WHERE id = ?")) {
            ps.setInt(1, clanId); ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("level");
        } catch (SQLException ignored) {} return 1;
    }

    public void addXp(int clanId, int xp) {
        try {
            PreparedStatement ps = db.getConnection().prepareStatement("SELECT level, xp FROM clans WHERE id = ?");
            ps.setInt(1, clanId); ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int currentXp = rs.getInt("xp") + xp;
                int level = rs.getInt("level");
                int needed = level * 1000;
                if (currentXp >= needed) {
                    level++; currentXp -= needed;
                    Bukkit.broadcastMessage("§6[Кланы] §eКлан " + clanNames.get(clanId) + " повысил уровень до " + level + "!");
                }
                PreparedStatement up = db.getConnection().prepareStatement("UPDATE clans SET level = ?, xp = ? WHERE id = ?");
                up.setInt(1, level); up.setInt(2, currentXp); up.setInt(3, clanId); up.executeUpdate();
            }
        } catch (SQLException ignored) {}
    }

    public String getChestData(int clanId) {
        try (PreparedStatement ps = db.getConnection().prepareStatement("SELECT chest_data FROM clans WHERE id = ?")) {
            ps.setInt(1, clanId); ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getString("chest_data");
        } catch (SQLException ignored) {} return "";
    }

    public void saveChestData(int clanId, String data) {
        try (PreparedStatement ps = db.getConnection().prepareStatement("UPDATE clans SET chest_data = ? WHERE id = ?")) {
            ps.setString(1, data); ps.setInt(2, clanId); ps.executeUpdate();
        } catch (SQLException ignored) {}
    }

    public void setPrefix(int clanId, String left, String text, String right) {
        try (PreparedStatement ps = db.getConnection().prepareStatement("UPDATE clans SET prefix_left = ?, prefix_text = ?, prefix_right = ? WHERE id = ?")) {
            ps.setString(1, left); ps.setString(2, text); ps.setString(3, right); ps.setInt(4, clanId); ps.executeUpdate();
        } catch (SQLException ignored) {}
    }

    public String getFormattedPrefix(int clanId) {
        try (PreparedStatement ps = db.getConnection().prepareStatement("SELECT prefix_left, prefix_text, prefix_right FROM clans WHERE id = ?")) {
            ps.setInt(1, clanId); ResultSet rs = ps.executeQuery();
            if (rs.next() && !rs.getString("prefix_text").isEmpty()) {
                return rs.getString("prefix_left") + rs.getString("prefix_text") + rs.getString("prefix_right") + " ";
            }
        } catch (SQLException ignored) {} return "";
    }

    public void setHome(Player p, int clanId) {
        Location l = p.getLocation();
        try (PreparedStatement ps = db.getConnection().prepareStatement("UPDATE clans SET home_world=?, home_x=?, home_y=?, home_z=? WHERE id=?")) {
            ps.setString(1, l.getWorld().getName()); ps.setDouble(2, l.getX()); ps.setDouble(3, l.getY()); ps.setDouble(4, l.getZ()); ps.setInt(5, clanId); ps.executeUpdate();
        } catch (SQLException ignored) {}
    }

    public Location getHome(int clanId) {
        try (PreparedStatement ps = db.getConnection().prepareStatement("SELECT home_world, home_x, home_y, home_z FROM clans WHERE id = ?")) {
            ps.setInt(1, clanId); ResultSet rs = ps.executeQuery();
            if (rs.next() && rs.getString("home_world") != null) {
                World w = Bukkit.getWorld(rs.getString("home_world"));
                return new Location(w, rs.getDouble("home_x"), rs.getDouble("home_y"), rs.getDouble("home_z"));
            }
        } catch (SQLException ignored) {} return null;
    }

    public Integer getClanId(UUID u) { return playerClan.get(u); }
    public String getRole(UUID u) { return playerRole.get(u); }
    public String getTag(int id) { return clanTags.get(id); }
    public String getName(int id) { return clanNames.get(id); }
}
