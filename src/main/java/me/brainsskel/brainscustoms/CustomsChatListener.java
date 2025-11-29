package me.brainsskel.brainscustoms;

import io.papermc.paper.chat.ChatRenderer;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.cacheddata.CachedMetaData;
import net.luckperms.api.event.user.UserDataRecalculateEvent;
import net.luckperms.api.model.user.User;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import net.kyori.adventure.text.Component;

import java.awt.*;

public class CustomsChatListener implements Listener, ChatRenderer {

    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private final LuckPerms luckPerms;

    private static CustomsChatListener instance;
    public static CustomsChatListener get() {
        return instance;
    }

 //--------------------------------//


    public CustomsChatListener(LuckPerms luckPerms) {
        instance = this;
        this.luckPerms = luckPerms;

    }

    //------------------------//

    @EventHandler
    public void onChat(AsyncChatEvent event) {
        event.renderer(this); // Tell the event to use our renderer
    }

    @Override
    public Component render(Player source, Component sourceDisplayName, Component message, Audience viewer) {
        String format = BrainsCustoms.getInstance().getConfig().getString("chat-format");
        User user = BrainsCustoms.getLuckPerms().getPlayerAdapter(Player.class).getUser(source);
        CachedMetaData meta = user.getCachedData().getMetaData();

        String prefix = meta.getPrefix() == null ? "" : meta.getPrefix();
        String rank = prefix;





        String result = format
                .replace("{PLAYER}", source.getName())
                .replace("{MESSAGE}", PlainTextComponentSerializer.plainText().serialize(message))
                .replace("{RANK}", rank);
        if (prefix.contains("&")){
            return LegacyComponentSerializer.legacyAmpersand().deserialize(result);
        } else {
            return miniMessage.deserialize(result);
        }

    }
}
