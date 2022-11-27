package me.plugin.serene.core.command;

import me.plugin.serene.statistics.CustomStatistic;
import me.plugin.serene.statistics.DeathStatistic;
import me.plugin.serene.statistics.StatisticsRepository;
import me.plugin.serene.statistics.TimePlayedStatistic;
import org.bukkit.Server;
import org.bukkit.Statistic;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class StatsCommand implements CommandExecutor {

    private final StatisticsRepository statisticsRepository;

    public StatsCommand() {
        statisticsRepository = new StatisticsRepository()
                .registerStatistic(new DeathStatistic())
                .registerStatistic(new TimePlayedStatistic());
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String statName = String.join(" ", args);
        CustomStatistic statistic = statisticsRepository.getStatisticForString(statName);
        Server server = sender.getServer();
        if (statistic == null) {
            sender.sendMessage("Unknown statistic %s".formatted(statName));
        } else {
            Statistic originalStatistic = statistic.getStatistic();
            Player player = server.getPlayer(sender.getName());
            if (player == null) {
                sender.sendMessage("Cannot execute this command from console");
            } else {
                int statValue = player.getStatistic(originalStatistic);
                sender.sendMessage("You have %d %s".formatted(statValue, statistic.getMessageSubject()));
            }
        }
        return true;
    }
}
