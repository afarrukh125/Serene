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
        Player player = server.getPlayer(sender.getName());
        if (player == null) {
            sender.sendMessage("Cannot execute this command from console");
        } else {
            if (statistic == null) {
                sender.sendMessage("Unknown statistic %s".formatted(statName));
            } else {
                Statistic originalStatistic = statistic.getStatistic();

                int statValue = player.getStatistic(originalStatistic);
                double translatedValue = statistic.translateValue(statValue);
                statistic.customMessage(translatedValue).ifPresentOrElse(sender::sendMessage,
                        () -> sender.sendMessage("You have %f %s".formatted(translatedValue, statistic.getMessageSubject())));

            }
        }
        return true;
    }
}
