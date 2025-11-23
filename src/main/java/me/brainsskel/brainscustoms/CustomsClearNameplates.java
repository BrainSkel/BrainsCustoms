package me.brainsskel.brainscustoms;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.TextDisplay;
import org.jetbrains.annotations.NotNull;

public class CustomsClearNameplates implements CommandExecutor {
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        for (World world : Bukkit.getWorlds()) {
            world.getEntities().stream()
                    .filter(e -> e instanceof TextDisplay)
                    .forEach(Entity::remove);
        }

        return false;
    }
}
