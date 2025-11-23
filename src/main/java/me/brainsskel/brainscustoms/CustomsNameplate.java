package me.brainsskel.brainscustoms;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class CustomsNameplate implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cOnly players can use this command.");
            return true;
        }

        // Let NameplateManager handle everything (spawn, follow, cleanup)
        if (args.length < 1) {
            NameplateManager.get().create(player);
        } else {
            Player target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                sender.sendMessage("&cThat player is not online!");
                return true;
            }
            NameplateManager.get().create(target);
        }


        player.sendMessage("§aNameplate updated!");
        return true;
    }
}