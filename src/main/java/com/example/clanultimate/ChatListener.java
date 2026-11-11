package com.example.clans.listeners;

import com.example.clans.manager.ClanManager;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class ChatListener implements Listener {
    private final ClanManager mgr;
    public ChatListener(ClanManager mgr) { this.mgr = mgr; }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onChat(AsyncChatEvent event) {
        Player p = event.getPlayer();
        Integer cid = mgr.getClanId(p.getUniqueId());
        if (cid == null) return;
        String prefix = mgr.getFormattedPrefix(cid);
        if (prefix.isEmpty()) return;

        Component prefixComponent = LegacyComponentSerializer.legacySection().deserialize(prefix);
        event.renderer((source, sourceDisplayName, message, viewer) -> 
            prefixComponent.append(sourceDisplayName).append(Component.text(": ")).append(message)
        );
    }
}