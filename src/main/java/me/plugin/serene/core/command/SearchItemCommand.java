package me.plugin.serene.core.command;

import static java.lang.String.join;
import static java.util.Objects.requireNonNull;

import me.plugin.serene.actions.ItemSearcher;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class SearchItemCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args == null || args.length == 0) {
            return false;
        }
        var player = requireNonNull(sender.getServer().getPlayer(sender.getName()));
        var originalLocation = player.getLocation();
        var world = player.getWorld();
        var targetItemParam = join(" ", args);
        var itemSearcher = new ItemSearcher();
        itemSearcher.searchItem(player, originalLocation, world, targetItemParam);
        return true;
    }
}
