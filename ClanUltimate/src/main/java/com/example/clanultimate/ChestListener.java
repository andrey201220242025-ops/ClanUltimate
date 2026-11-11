package com.example.clans.listeners;

import com.example.clans.ClansPlugin;
import com.example.clans.commands.ClanCommand;
import com.example.clans.manager.ClanManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.ByteArrayOutputStream;

public class ChestListener implements Listener {
    private final ClansPlugin plugin;
    private final ClanManager mgr;
    private final ClanCommand clanCommand;

    public ChestListener(ClansPlugin plugin, ClanManager mgr, ClanCommand clanCommand) {
        this.plugin = plugin;
        this.mgr = mgr;
        this.clanCommand = clanCommand;
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        Inventory inv = event.getInventory();
        // Проверяем, есть ли на сундуке наша метка
        if (inv.getPersistentDataContainer().has(clanCommand.CLAN_CHEST_KEY, PersistentDataType.INTEGER)) {
            int cid = inv.getPersistentDataContainer().get(clanCommand.CLAN_CHEST_KEY, PersistentDataType.INTEGER);
            
            try {
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);
                dataOutput.writeInt(inv.getSize());
                for (ItemStack item : inv.getContents()) {
                    dataOutput.writeObject(item);
                }
                dataOutput.close();
                String encoded = Base64Coder.encodeLines(outputStream.toByteArray());
                mgr.saveChestData(cid, encoded);
            } catch (Exception e) {
                plugin.getLogger().warning("Ошибка сохранения сундука: " + e.getMessage());
            }
        }
    }
}