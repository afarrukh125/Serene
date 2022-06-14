package me.plugin.serene.core.command;


import me.plugin.serene.database.SereneDatabaseClient;
import me.plugin.serene.exceptions.SereneCommandException;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import static java.util.Objects.requireNonNull;

public class ToggleVeinBreakerCommand implements CommandExecutor {

    private static final String NAME = "veinbreaker";

    private final SereneDatabaseClient database;

    public ToggleVeinBreakerCommand(SereneDatabaseClient database) {
        this.database = database;
    }

    private boolean parseParam(String param) throws SereneCommandException {
        return switch (param.toLowerCase()) {
            case "enabled", "on" -> true;
            case "disabled", "off" -> false;
            default -> throw new SereneCommandException("Please enter on or off as a parameter");
        };
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0)
            sender.sendMessage("Usage: %s (on|off) ".formatted(NAME));
        String param = args[0];
        try {
            boolean enabled = parseParam(param);
            var player = sender.getServer().getPlayer(sender.getName());
            database.setVeinBreakerEnabled(requireNonNull(player), enabled);
            player.sendMessage("Veinbreaker is now %s".formatted(enabled ? "enabled" : "disabled"));
        } catch (SereneCommandException e) {
            sender.sendMessage("Usage: %s (on|off) ".formatted(NAME));
        } catch (NullPointerException e) {
            sender.sendMessage("Only players in game can invoke this command");
        }
        return true;
    }
}
