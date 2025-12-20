package me.brainsskel.brainscustoms;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class CustomsNameplateReloadPlugin implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage("&cThat player is not online!");
            return true;
        }
        NameplateManager.get().reloadNameplate(target);
        return true;
    }
}
