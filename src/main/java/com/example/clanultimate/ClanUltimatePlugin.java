package com.example.clanultimate;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class ClanUltimatePlugin extends JavaPlugin {
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
        
        getCommand("clan").setExecutor(new ClanCommand(this, clanManager));
        getCommand("cc").setExecutor(new ClanChatCommand(this, clanManager));
        getServer().getPluginManager().registerEvents(new ChatListener(clanManager), this);
        getServer().getPluginManager().registerEvents(new ChestListener(this, clanManager), this);
        
        getLogger().info("ClanUltimate enabled!");
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
