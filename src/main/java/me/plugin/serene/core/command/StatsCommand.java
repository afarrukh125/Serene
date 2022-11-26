package me.plugin.serene.core.command;

import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.Statistic;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static java.lang.Runtime.getRuntime;
import static java.util.concurrent.Executors.newFixedThreadPool;

public class StatsCommand implements CommandExecutor {

    private static final Logger LOG = LoggerFactory.getLogger(StatsCommand.class);
    private static final long LEADERBOARD_LIMIT = 5;
    private static final Map<Statistic, Function<Integer, Integer>> SPECIAL_STATISTICS = Map.of(Statistic.PLAY_ONE_MINUTE, x -> x / 20 / 60 / 60);

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Map<Statistic, String> resultStrings = new ConcurrentHashMap<>();
        var executor = newFixedThreadPool(getRuntime().availableProcessors() * 2);
        Server server = sender.getServer();
        OfflinePlayer[] allPlayers = server.getOfflinePlayers();
        for (Statistic statistic : Arrays.stream(Statistic.values()).filter(s -> !s.isSubstatistic()).toList()) {
            executor.submit(() -> {
                String leaderboardString = getLeaderBoardStringForStatistic(statistic, allPlayers);
                resultStrings.put(statistic, leaderboardString.trim());
            });
        }
        executor.shutdown();
        try {
            executor.awaitTermination(1, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        resultStrings.keySet().stream()
                .sorted(Comparator.comparing(Enum::name))
                .map(s -> "\n" + statisticToName(s) + "\n" + resultStrings.get(s))
                .forEach(System.out::println);
        return true;
    }

    private static String getLeaderBoardStringForStatistic(Statistic statistic, OfflinePlayer[] allPlayers) {
        StringBuilder sb = new StringBuilder();
        LOG.info("Using thread {} for statistic {}", Thread.currentThread().getName(), statistic);
        List<OfflinePlayer> players = Arrays.stream(allPlayers)
                .sorted((o1, o2) -> o2.getStatistic(statistic) - o1.getStatistic(statistic))
                .limit(LEADERBOARD_LIMIT)
                .toList();

        int rank = 1;
        for (OfflinePlayer player : players) {
            int result = parseStatistic(statistic, player);
            sb.append("%d. %s - %d\n".formatted(rank++, player.getName(), result));
        }
        return sb.toString();
    }

    private static String statisticToName(Statistic statistic) {
        return statistic.getKey().toString().replace("minecraft:", "").replace("_", " ");
    }

    private static int parseStatistic(Statistic statistic, OfflinePlayer player) {
        int originalStatistic = player.getStatistic(statistic);
        if (SPECIAL_STATISTICS.containsKey(statistic)) {
            return SPECIAL_STATISTICS.get(statistic).apply(originalStatistic);
        }
        return originalStatistic;
    }
}
