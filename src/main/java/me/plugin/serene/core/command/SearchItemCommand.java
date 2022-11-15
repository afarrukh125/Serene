package me.plugin.serene.core.command;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;

import static java.lang.String.join;
import static java.util.Objects.requireNonNull;

public class SearchItemCommand implements CommandExecutor {

    private static final double MAX_DISTANCE_TO_SEARCH = 5;
    private final FileConfiguration configuration;

    private static final Logger LOG = LoggerFactory.getLogger(SearchItemCommand.class);

    public SearchItemCommand(FileConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player player = requireNonNull(sender.getServer().getPlayer(sender.getName()));
        Location originalLocation = player.getLocation();
        World world = player.getWorld();
        String targetItemParam = join(" ", args);
        parseTargetMaterialFromParam(targetItemParam).ifPresentOrElse(targetMaterial -> {
            Queue<Location> locationsToSearch = new LinkedList<>();
            List<Chest> foundChests = new ArrayList<>();
            Set<Chest> seenChests = new HashSet<>();
            locationsToSearch.add(originalLocation);

            while (!locationsToSearch.isEmpty()) {
                var nextLocation = locationsToSearch.poll();
                Block block = nextLocation.getBlock();
                boolean wasChest = requireNonNull(block.getType()).equals(Material.CHEST);
                if (wasChest) {
                    var chest = (Chest) block.getState();
                    Inventory inventory = chest.getInventory();
                    if (!inventory.isEmpty() && inventory.contains(targetMaterial) && !seenChests.contains(chest)) {
                        foundChests.add(chest);
                        seenChests.add(chest);
                    }
                }

                if (nextLocation.getX() - originalLocation.getX() < MAX_DISTANCE_TO_SEARCH &&
                    nextLocation.getY() - originalLocation.getY() < MAX_DISTANCE_TO_SEARCH &&
                    nextLocation.getZ() - originalLocation.getZ() < MAX_DISTANCE_TO_SEARCH) {
                    locationsToSearch.add(new Location(world, nextLocation.getX() + 1, nextLocation.getY(), nextLocation.getZ()));
                    locationsToSearch.add(new Location(world, nextLocation.getX(), nextLocation.getY() + 1, nextLocation.getZ()));
                    locationsToSearch.add(new Location(world, nextLocation.getX(), nextLocation.getY(), nextLocation.getZ() + 1));

                }
            }

            LOG.info("Found {} chests with item {}", foundChests.size(), targetMaterial);
            for (Chest chest : foundChests) {
                player.playEffect(chest.getLocation(), Effect.BONE_MEAL_USE, 10);
            }
        }, () -> player.sendMessage("Could not match %s to an item".formatted(targetItemParam)));
        return true;
    }

    private Optional<Material> parseTargetMaterialFromParam(String targetItemParam) {
        return Optional.ofNullable(Material.matchMaterial(targetItemParam));
    }
}
