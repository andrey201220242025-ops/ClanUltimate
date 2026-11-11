package com.example.clans;

import com.example.clans.commands.ClanChatCommand;
import com.example.clans.commands.ClanCommand;
import com.example.clans.db.Database;
import com.example.clans.listeners.ChatListener;
import com.example.clans.listeners.ChestListener;
import com.example.clans.manager.ClanManager;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class ClansPlugin extends JavaPlugin {
    private Database database;
    private ClanManager clanManager;
    private Economy economy;

    @Override
    public void onEnable() {
        if (!setupEconomy()) getLogger().severe("Vault не найден! Экономика отключена.");

        database = new Database(this);
        try {
            database.connect();
            database.createTables();
        } catch (Exception e) {
            getLogger().severe("Ошибка БД: " + e.getMessage());
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        clanManager = new ClanManager(this, database);
        ClanCommand clanCmd = new ClanCommand(this, clanManager);
        
        getCommand("clan").setExecutor(clanCmd);
        getCommand("cc").setExecutor(new ClanChatCommand(this, clanManager));
        
        getServer().getPluginManager().registerEvents(new ChatListener(clanManager), this);
        getServer().getPluginManager().registerEvents(new ChestListener(this, clanManager, clanCmd), this);
        
        getLogger().info("ClansPlugin enabled!");
    }

    @Override
    public void onDisable() {
        if (database != null) database.disconnect();
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) return false;
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) return false;
        economy = rsp.getProvider();
        return economy != null;
    }

    public Economy getEconomy() { return economy; }
}