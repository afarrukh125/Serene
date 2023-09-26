package me.plugin.serene.core.command;

import me.plugin.serene.actions.ItemSearcher;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static java.lang.String.join;
import static java.util.Objects.requireNonNull;

public class SearchItemCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args == null || args.length == 0) {
            return false;
        }
        Player player = requireNonNull(sender.getServer().getPlayer(sender.getName()));
        Location originalLocation = player.getLocation();
        World world = player.getWorld();
        String targetItemParam = join(" ", args);
        ItemSearcher itemSearcher = new ItemSearcher(targetItemParam);
        itemSearcher.searchItem(player, originalLocation, world);
        return true;
    }
}
