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
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.Transformation;
import org.jetbrains.annotations.NotNull;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.*;

import java.awt.*;
import java.awt.Color;
import java.util.List;

import static org.apache.logging.log4j.status.StatusLogger.getLogger;


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

    public void clearAllNameplates() {
        for (World world : Bukkit.getWorlds()) {
            world.getEntities().stream()
                    .filter(e -> e instanceof TextDisplay)
                    .forEach(Entity::remove);
        }



    }

    // -------------------------
    // Create or Update Nameplate
    // -------------------------
    public void create(Player player) {

        // load config safely
        Yaml yaml = new Yaml();
        InputStream in = getClass().getClassLoader().getResourceAsStream("config.yml");
        Map<String, Object> config = Collections.emptyMap();
        if (in != null) {
            Object loaded = yaml.load(in);
            if (loaded instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> tmp = (Map<String, Object>) loaded;
                config = tmp;
            }
        }

        // defensive cast: rank-animations may be missing or not the shape we expect
        Map<String, Map<String, List<String>>> animations = Collections.emptyMap();
        Object rawAnimations = config.get("rank-animations");
        if (rawAnimations instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> top = (Map<String, Object>) rawAnimations;
            Map<String, Map<String, List<String>>> safe = new HashMap<>();
            for (Map.Entry<String, Object> e : top.entrySet()) {
                String rankKey = e.getKey();
                Object inner = e.getValue();
                if (inner instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> innerMap = (Map<String, Object>) inner;
                    Object framesObj = innerMap.get("frames");
                    if (framesObj instanceof List) {
                        @SuppressWarnings("unchecked")
                        List<Object> rawList = (List<Object>) framesObj;
                        List<String> frames = new ArrayList<>();
                        for (Object o : rawList) {
                            if (o != null) frames.add(String.valueOf(o));
                        }
                        Map<String, List<String>> m = new HashMap<>();
                        m.put("frames", List.copyOf(frames));
                        safe.put(rankKey, m);
                    } else {
                        // no frames key or not a list -> store empty frames list
                        Map<String, List<String>> m = new HashMap<>();
                        m.put("frames", List.of());
                        safe.put(rankKey, m);
                    }
                } else {
                    // rank entry exists but is not a map -> treat as empty frames
                    Map<String, List<String>> m = new HashMap<>();
                    m.put("frames", List.of());
                    safe.put(rankKey, m);
                }
            }
            animations = safe;
        }

        // remove old if exists
        remove(player);

        // hide vanilla name tag
        hideVanillaNameTag(player);

        // get LuckPerms prefix
        User user = BrainsCustoms.getLuckPerms().getPlayerAdapter(Player.class).getUser(player);
        CachedMetaData meta = user.getCachedData().getMetaData();

        String prefix = meta.getPrefix() == null ? "" : meta.getPrefix();
        String suffix = meta.getSuffix() == null ? "" : meta.getSuffix();
        String playerRank = meta.getPrimaryGroup();

        // prepare frames safely
        List<String> frames = new ArrayList<>();
        if (prefix != null && !prefix.isEmpty()) frames.add(prefix);
        else frames.add(player.getName());

        if (animations != null && animations.containsKey(playerRank)) {
            List<String> cfgFrames = animations.get(playerRank).get("frames");
            if (cfgFrames != null && !cfgFrames.isEmpty()) {
                frames = new ArrayList<>(cfgFrames);
            }
        }
        final List<String> Finalframes = frames.isEmpty() ? List.of(player.getName()) : List.copyOf(frames);

        // initial component for rank (use first frame safely)
        Component rank;
        try {
            rank = MiniMessage.miniMessage().deserialize(Finalframes.get(0));
        } catch (Exception ex) {
            rank = Component.text(Finalframes.get(0));
        }
        final Component finalRank = rank;

        Component playerName = MiniMessage.miniMessage().deserialize("<gradient:#c9c9c9:#e8e8e8>" + player.getName() + "</gradient>");
        Location loc = player.getLocation().clone();
        loc.setPitch(90F);

        TextDisplay rankDisplay = player.getWorld().spawn(loc, TextDisplay.class, td -> {
            td.setBillboard(Display.Billboard.CENTER);
            td.setAlignment(TextDisplay.TextAlignment.CENTER);
            td.setShadowed(false);
            td.setBackgroundColor(null); // transparent background
            td.text(finalRank);
        });

        // animation task (use MiniMessage parsing per-frame but guarded)
        new BukkitRunnable() {
            int frame = 0;

            @Override
            public void run() {
                if (!player.isOnline() || rankDisplay.isDead()) {
                    cancel();
                    return;
                }

                String raw = Finalframes.get(frame);
                Component comp;
                try {
                    comp = MiniMessage.miniMessage().deserialize(raw);
                } catch (Exception ex) {
                    comp = Component.text(raw);
                }
                rankDisplay.text(comp);
                frame = (frame + 1) % Finalframes.size();
            }

        }.runTaskTimer(BrainsCustoms.getInstance(), 0, 2); // every 2 ticks

        TextDisplay playerNameDisplay = player.getWorld().spawn(loc, TextDisplay.class, td -> {
            td.setBillboard(Display.Billboard.CENTER);
            td.setAlignment(TextDisplay.TextAlignment.CENTER);
            td.setShadowed(true);
            td.text(playerName);
        });

        // correct offset ABOVE the head
        rankDisplay.setTransformation(
                new Transformation(
                        new Vector3f(0, 0.55f, -0.2F),
                        new AxisAngle4f(),
                        new Vector3f(1, 1, 1),
                        new AxisAngle4f()
                )
        );

        playerNameDisplay.setTransformation(
                new Transformation(
                        new Vector3f(0, 0.30f, -0.2F),
                        new AxisAngle4f(),
                        new Vector3f(1, 1, 1),
                        new AxisAngle4f()
                )
        );

        // make display follow player (your original approach)
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

        Bukkit.getScheduler().runTask(BrainsCustoms.getInstance(), () -> {

            for (TextDisplay td : list) {
                if (td != null && !td.isDead() && td.isValid()) {
                    td.remove();
                }
            }
        });
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
        clearAllNameplates();
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
        Bukkit.getScheduler().runTaskLater(BrainsCustoms.getInstance(), () ->
            remove(e.getPlayer()), 2L);

    }

    // Remove when dying
    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        Bukkit.getScheduler().runTaskLater(BrainsCustoms.getInstance(), () ->
                remove(e.getEntity()), 2L);

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
        //reloadAllNameplates();
        Bukkit.getScheduler().runTaskLater(BrainsCustoms.getInstance(), () ->
                create(e.getPlayer()), 5L);

    }

    @EventHandler
    public void changeDimension(PlayerChangedWorldEvent e) {
        Bukkit.getScheduler().runTaskLater(BrainsCustoms.getInstance(), () ->
                create(e.getPlayer()), 5L);
    }
}