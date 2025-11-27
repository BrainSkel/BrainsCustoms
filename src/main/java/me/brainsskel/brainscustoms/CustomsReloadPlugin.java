package me.brainsskel.brainscustoms;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class CustomsReloadPlugin implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        sender.sendMessage("&a Reloading BrainsCustoms");

        BrainsCustoms plugin = BrainsCustoms.getInstance();


        plugin.reloadConfig();
        NameplateManager.get().reloadAllNameplates();


        sender.sendMessage("&aReloaded BrainsCustoms!");
        return true;
    }


}
