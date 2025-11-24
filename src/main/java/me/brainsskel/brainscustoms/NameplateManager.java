package me.brainsskel.brainscustoms;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.cacheddata.CachedMetaData;
import net.luckperms.api.event.player.lookup.UsernameValidityCheckEvent;
import net.luckperms.api.event.user.UserDataRecalculateEvent;
import net.luckperms.api.model.user.User;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Display;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.Transformation;
import org.jetbrains.annotations.NotNull;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;
import java.util.List;

import java.awt.*;
import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;



public class NameplateManager implements Listener {

    private final LuckPerms luckPerms;
    private final Map<UUID, List<TextDisplay>> displays = new HashMap<>();

    // Singleton
    private static NameplateManager instance;
    public static NameplateManager get() {
        return instance;
    }
    //LuckPerms luckPerms = LuckPermsProvider.get();

    public NameplateManager(LuckPerms luckPerms) {
        instance = this;
        this.luckPerms = luckPerms;
        luckPerms.getEventBus().subscribe(UserDataRecalculateEvent.class, event -> {
            User user = event.getUser();
            Player target = Bukkit.getPlayer(user.getUsername());
            if (target ==null) return;

            Bukkit.getScheduler().runTaskLater(BrainsCustoms.getInstance(), () ->
                    create(target), 2L);
        });


        // Cleanup on server start (optional)
        Bukkit.getWorlds().forEach(world ->
                world.getEntitiesByClass(TextDisplay.class).forEach(TextDisplay::remove)
        );
    }

    // -------------------------
    // Create or Update Nameplate
    // -------------------------
    public void create(Player player) {

        // remove old if exists
        remove(player);

        // hide vanilla name tag
        hideVanillaNameTag(player);

        // get LuckPerms prefix
        User user = BrainsCustoms.getLuckPerms().getPlayerAdapter(Player.class).getUser(player);
        CachedMetaData meta = user.getCachedData().getMetaData();
        String prefix = meta.getPrefix() == null ? "" : meta.getPrefix();
        String suffix = meta.getSuffix() == null ? "" : meta.getSuffix();

        Component rank = LegacyComponentSerializer.legacyAmpersand().deserialize(prefix);

//        Component finalText = Component.text()
//                .append(rank)
//                .append(Component.newline())
//                .append(Component.text(player.getName(), NamedTextColor.WHITE))
//                .build();

        Component playerName = MiniMessage.miniMessage().deserialize("<gradient:#c9c9c9:#e8e8e8>"+ player.getName() +"</gradient>");
        Location loc = player.getLocation();

        TextDisplay rankDisplay = player.getWorld().spawn(loc, TextDisplay.class, td -> {
            td.setBillboard(Display.Billboard.CENTER);
            td.setAlignment(TextDisplay.TextAlignment.CENTER);
            td.setShadowed(false);
            td.setBackgroundColor(org.bukkit.Color.fromRGB(191, 247, 255));
            td.text(rank);
        });
        TextDisplay playerNameDisplay = player.getWorld().spawn(loc, TextDisplay.class, td -> {
            td.setBillboard(Display.Billboard.CENTER);
            td.setAlignment(TextDisplay.TextAlignment.CENTER);
            td.setShadowed(true);
            td.text(playerName);
            //td.setBackgroundColor(org.bukkit.Color.fromRGB(0,255,204)); // optional
        });


        // correct offset ABOVE the head
        rankDisplay.setTransformation(
                new Transformation(
                        new Vector3f(0, 0.50f, 0),
                        new AxisAngle4f(),
                        new Vector3f(1, 1, 1),
                        new AxisAngle4f()
                )
        );

        playerNameDisplay.setTransformation(
                new Transformation(
                        new Vector3f(0, 0.25f, 0),
                        new AxisAngle4f(),
                        new Vector3f(1, 1, 1),
                        new AxisAngle4f()
                )
        );

        // make display follow player
        player.addPassenger(rankDisplay);
        player.addPassenger(playerNameDisplay);

        displays.put(player.getUniqueId(), List.of(rankDisplay, playerNameDisplay));
    }



    // -------------------------
    // Remove a player's tag
    // -------------------------
    public void remove(Player player) {
        List<TextDisplay> list = displays.remove(player.getUniqueId());
        if (list == null) return;

        for (TextDisplay td : list) {
            if (!td.isDead()) td.remove();
        }
    }

    // -------------------------
    // Hide vanilla name tag
    // -------------------------
    private void hideVanillaNameTag(Player player) {
        Scoreboard board = Bukkit.getScoreboardManager().getMainScoreboard();
        Team team = board.getTeam("hidenametags");

        if (team == null) {
            team = board.registerNewTeam("hidenametags");
            team.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.NEVER);
        }

        team.addEntry(player.getName());
    }


    // ----------------
    // Reload everyones namepaltes
    // ----------------

    public void reloadAllNameplates() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            create(p);
        }
    }

    // -------------------------
    // Events for cleanup & recreation
    // -------------------------

//    public void permissionChanged() {
//        luckPerms.getEventBus().subscribe(UserDataRecalculateEvent.class, e -> {
//            User user = e.getUser();
//            Player target = Bukkit.getPlayer(user.getUsername());
//            if (target != null) {
//                create(target);
//            }
//        });
//    }

    // Remove when quitting
    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        remove(e.getPlayer());
    }

    // Remove when dying
    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        remove(e.getEntity());
    }

    // Recreate when respawning
    @EventHandler
    public void onRespawn(PlayerRespawnEvent e) {
        Bukkit.getScheduler().runTaskLater(BrainsCustoms.getInstance(), () ->
                create(e.getPlayer()), 2L);
    }

    // Optional: auto-create nameplate on join
    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Bukkit.getScheduler().runTaskLater(BrainsCustoms.getInstance(), () ->
                create(e.getPlayer()), 5L);
    }
}