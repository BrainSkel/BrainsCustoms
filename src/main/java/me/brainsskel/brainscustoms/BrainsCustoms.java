package me.brainsskel.brainscustoms;

import net.luckperms.api.LuckPerms;
import org.bukkit.Bukkit;
import org.bukkit.entity.TextDisplay;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class BrainsCustoms extends JavaPlugin implements Listener {
    private static LuckPerms luckPerms;
    private static BrainsCustoms instance;

    public static BrainsCustoms getInstance() { // <--- add this
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;
        new NameplateManager();
        displays.clear();

        // Register Events ------------------------------------------------
        getServer().getPluginManager().registerEvents(new PlayerQuitListener(), this);
        Bukkit.getPluginManager().registerEvents(NameplateManager.get(), this);

        // Register Commands ------------------------------------------------
        getCommand("CustomsClearNameplates").setExecutor(new CustomsClearNameplates());
        getCommand("CustomsNameplate").setExecutor(new CustomsNameplate());

        getLogger().info("Brains Customs has been enabled!"); // Announce server has started

        // Implement luckperms
        RegisteredServiceProvider<LuckPerms> provider = Bukkit.getServicesManager().getRegistration(LuckPerms.class);
        if (provider != null) {
            luckPerms = provider.getProvider();
            getLogger().info("LuckPerms API hooked successfully!");
        } else {
            getLogger().warning("Could not hook into LuckPerms!");
        }
    }
    public static LuckPerms getLuckPerms() {
        return luckPerms;
    }

    public static final Map<UUID, TextDisplay> displays = new HashMap<>();



    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event){
        System.out.println("[---EESTISADAM---]");
    }




    @Override
    public void onDisable() {
        getLogger().info("Brains Customs has been disabled!");
    }

}
