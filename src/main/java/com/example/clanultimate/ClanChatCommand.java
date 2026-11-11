package com.example.clans.commands;

import com.example.clans.ClansPlugin;
import com.example.clans.manager.ClanManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ClanChatCommand implements CommandExecutor {
    private final ClanManager mgr;
    public ClanChatCommand(ClansPlugin plugin, ClanManager mgr) { this.mgr = mgr; }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player p)) return true;
        Integer cid = mgr.getClanId(p.getUniqueId());
        if (cid == null) { p.sendMessage("§cВы не в клане."); return true; }
        if (args.length == 0) { p.sendMessage("§c/cc <сообщение>"); return true; }

        String msg = String.join(" ", args);
        String format = "§8[§6" + mgr.getTag(cid) + "§8] §f" + p.getName() + "§7: §f" + msg;
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (cid.equals(mgr.getClanId(online.getUniqueId()))) online.sendMessage(format);
        }
        return true;
    }
}