package me.brainsskel.brainscustoms;

import org.bukkit.entity.TextDisplay;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

public class PlayerQuitListener implements Listener {

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        TextDisplay display = BrainsCustoms.displays.remove(uuid);
        if (display != null && !display.isDead()) {
            display.remove();
        }
    }
}
