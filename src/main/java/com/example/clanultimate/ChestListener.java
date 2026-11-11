package com.example.clanultimate;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ChestListener implements Listener {
    private final ClanUltimatePlugin plugin;
    private final ClanManager mgr;
    public static final Map<UUID, Integer> openChests = new HashMap<>();

    public ChestListener(ClanUltimatePlugin plugin, ClanManager mgr) {
        this.plugin = plugin;
        this.mgr = mgr;
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player p)) return;
        if (openChests.containsKey(p.getUniqueId())) {
            int cid = openChests.remove(p.getUniqueId());
            Inventory inv = event.getInventory();
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
