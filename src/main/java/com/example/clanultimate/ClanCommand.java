package com.example.clanultimate;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.ByteArrayInputStream;

public class ClanCommand implements CommandExecutor {
    private final ClanUltimatePlugin plugin;
    private final ClanManager mgr;

    public ClanCommand(ClanUltimatePlugin plugin, ClanManager mgr) {
        this.plugin = plugin;
        this.mgr = mgr;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player p)) return true;
        if (args.length == 0) { p.sendMessage("§6/clan create, invite, accept, invest, deposit, chest, sethome, home, setprefix"); return true; }

        String sub = args[0].toLowerCase();
        Integer cid = mgr.getClanId(p.getUniqueId());

        switch (sub) {
            case "create" -> {
                if (args.length < 3) { p.sendMessage("§c/clan create <имя> <тег>"); return true; }
                p.sendMessage(mgr.createClan(p, args[1], args[2]));
            }
            case "invite" -> {
                if (args.length < 2) return true;
                Player t = Bukkit.getPlayerExact(args[1]);
                if (t == null) { p.sendMessage("§cНе в сети."); return true; }
                if (cid == null || !"OWNER".equals(mgr.getRole(p.getUniqueId()))) { p.sendMessage("§cТолько лидер."); return true; }
                mgr.invite(t, cid);
                p.sendMessage("§aПриглашен.");
                t.sendMessage("§aВас пригласили! /clan accept");
            }
            case "accept" -> p.sendMessage(mgr.acceptInvite(p));
            case "invest" -> {
                if (cid == null) return true;
                if (args.length < 2) { p.sendMessage("§c/clan invest <сумма>"); return true; }
                Economy eco = plugin.getEconomy();
                double amount;
                try { amount = Double.parseDouble(args[1]); } catch (Exception e) { p.sendMessage("§cЧисло!"); return true; }
                if (eco.getBalance(p) < amount) { p.sendMessage("§cМало денег."); return true; }
                eco.withdrawPlayer(p, amount);
                mgr.setBalance(cid, mgr.getBalance(cid) + amount);
                p.sendMessage("§aВы пополнили казну на " + amount);
            }
            case "deposit" -> {
                if (cid == null) return true;
                if (!"OWNER".equals(mgr.getRole(p.getUniqueId()))) { p.sendMessage("§cТолько лидер."); return true; }
                if (args.length < 2) { p.sendMessage("§c/clan deposit <сумма>"); return true; }
                Economy eco = plugin.getEconomy();
                double amount;
                try { amount = Double.parseDouble(args[1]); } catch (Exception e) { p.sendMessage("§cЧисло!"); return true; }
                if (mgr.getBalance(cid) < amount) { p.sendMessage("§cВ казне мало денег."); return true; }
                mgr.setBalance(cid, mgr.getBalance(cid) - amount);
                eco.depositPlayer(p, amount);
                p.sendMessage("§aВы сняли " + amount + " из казны.");
            }
            case "chest" -> {
                if (cid == null) { p.sendMessage("§cВы не в клане."); return true; }
                int size = Math.min(54, mgr.getLevel(cid) * 9);
                Inventory inv = Bukkit.createInventory(null, size, "§8Сундук клана");

                String data = mgr.getChestData(cid);
                if (data != null && !data.isEmpty()) {
                    try {
                        ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(data));
                        BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);
                        for (int i = 0; i < size; i++) {
                            try { inv.setItem(i, (ItemStack) dataInput.readObject()); } catch (Exception e) { break; }
                        }
                        dataInput.close();
                    } catch (Exception ignored) {}
                }
                ChestListener.openChests.put(p.getUniqueId(), cid);
                p.openInventory(inv);
            }
            case "sethome" -> {
                if (cid == null || !"OWNER".equals(mgr.getRole(p.getUniqueId()))) return true;
                mgr.setHome(p, cid);
                p.sendMessage("§aТочка дома установлена.");
            }
            case "home" -> {
                if (cid == null) return true;
                Location loc = mgr.getHome(cid);
                if (loc == null) { p.sendMessage("§cДом не установлен."); return true; }
                p.teleport(loc);
                p.sendMessage("§aТелепортация на базу...");
            }
            case "setprefix" -> {
                if (cid == null || !"OWNER".equals(mgr.getRole(p.getUniqueId()))) { p.sendMessage("§cТолько лидер."); return true; }
                if (args.length < 4) { p.sendMessage("§c/clan setprefix <L> <TEXT> <R>"); return true; }
                String l = args[1].replace('&', '§');
                String t = args[2].replace('&', '§');
                String r = args[3].replace('&', '§');
                if (t.replaceAll("§[a-zA-Z0-9]", "").length() > 5) { p.sendMessage("§cМаксимум 5 символов!"); return true; }
                mgr.setPrefix(cid, l, t, r);
                p.sendMessage("§aПрефикс установлен!");
            }
        }
        return true;
    }
}
