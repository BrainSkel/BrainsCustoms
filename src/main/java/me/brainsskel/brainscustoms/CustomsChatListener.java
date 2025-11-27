package me.brainsskel.brainscustoms;

import io.papermc.paper.chat.ChatRenderer;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import net.kyori.adventure.text.Component;

import java.awt.*;

public class CustomsChatListener implements Listener, ChatRenderer {

    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private final BrainsCustoms plugin;
    public CustomsChatListener(BrainsCustoms plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onChat(AsyncChatEvent event) {
        event.renderer(this); // Tell the event to use our renderer
    }

    @Override
    public Component render(Player source, Component sourceDisplayName, Component message, Audience viewer) {
        String format = plugin.getConfig().getString("chat-format");

        String result = format
                .replace("{PLAYER}", source.getName())
                .replace("{MESSAGE}", PlainTextComponentSerializer.plainText().serialize(message))
                .replace("RANK", "Role");
        return miniMessage.deserialize(result);
    }
}
