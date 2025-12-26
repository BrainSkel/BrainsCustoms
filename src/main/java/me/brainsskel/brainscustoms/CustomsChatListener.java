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

    private static final String[][] LEGACY_TO_MINIMESSAGE = {
            {"&0", "<black>"}, {"&1", "<dark_blue>"}, {"&2", "<dark_green>"}, {"&3", "<dark_aqua>"},
            {"&4", "<dark_red>"}, {"&5", "<dark_purple>"}, {"&6", "<gold>"}, {"&7", "<gray>"},
            {"&8", "<dark_gray>"}, {"&9", "<blue>"}, {"&a", "<green>"}, {"&b", "<aqua>"},
            {"&c", "<red>"}, {"&d", "<light_purple>"}, {"&e", "<yellow>"}, {"&f", "<white>"},
            {"&k", "<obfuscated>"}, {"&l", "<bold>"}, {"&m", "<strikethrough>"}, {"&n", "<underline>"},
            {"&o", "<italic>"}, {"&r", "<reset>"}
    };

    public static String convertLegacyToMiniMessage(String legacyMessage) {
        String miniMessageFormatted = legacyMessage;

        // Loop through all Legacy to MiniMessage color code mappings
        for (String[] mapping : LEGACY_TO_MINIMESSAGE) {
            miniMessageFormatted = miniMessageFormatted.replace(mapping[0], mapping[1]);
        }

        // Return the MiniMessage Component
        return miniMessageFormatted;
    }


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

        Component prefixComponent;
        if (prefix.contains("<")) {
            prefixComponent = miniMessage.deserialize(prefix)
                    .append(MiniMessage.miniMessage().deserialize("<reset>"));
        } else {
            prefixComponent = LegacyComponentSerializer.legacyAmpersand().deserialize(prefix + "&r"); // Bukkit colors
        }

        String result = format
                .replace("{PLAYER}", source.getName())
                .replace("{MESSAGE}", PlainTextComponentSerializer.plainText().serialize(message))
                .replace("{RANK}", convertLegacyToMiniMessage(prefix));






//        if (prefix.contains("&")){
//            return LegacyComponentSerializer.legacyAmpersand().deserialize(result + "&r");
//        } else {
//            return miniMessage.deserialize(result);
//        }
        return miniMessage.deserialize(convertLegacyToMiniMessage(result));

    }
}
