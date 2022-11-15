package me.plugin.serene.actions;

import me.plugin.serene.database.SereneDatabaseClient;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.player.PlayerExpChangeEvent;

import static java.util.Objects.requireNonNull;

public class ExperienceHandler {


    private static final int BONUS = 100;
    private static final int REWARD_THRESHOLD = 100;
    private static final int MINIMUM_LEVEL_FOR_BONUS = 20;
    private static final int MAXIMUM_LEVEL_FOR_BONUS = 30;

    private final SereneDatabaseClient databaseClient;
    private final FileConfiguration config;

    public ExperienceHandler(SereneDatabaseClient databaseClient, FileConfiguration config) {
        this.databaseClient = databaseClient;
        this.config = config;
    }

    public void handleEvent(PlayerExpChangeEvent playerExpChangeEvent) {
        var player = playerExpChangeEvent.getPlayer();
        var level = player.getLevel();
        if (level > MINIMUM_LEVEL_FOR_BONUS && level < MAXIMUM_LEVEL_FOR_BONUS) {
            var originalAmount = playerExpChangeEvent.getAmount();
            databaseClient.addExperienceForPlayer(player, originalAmount);
            var experienceForPlayer = databaseClient.getExperienceForPlayer(player);
            if (experienceForPlayer >= REWARD_THRESHOLD) {
                var additional = BONUS + (2 * level);
                var finalAmount = originalAmount + additional;
                playerExpChangeEvent.setAmount(finalAmount);
                databaseClient.setExperienceForPlayer(player, Math.max(0, experienceForPlayer - REWARD_THRESHOLD));
                if (requireNonNull(config.getString("experience.showmessage")).equals("enabled")) {
                    player.sendMessage("A bonus of %s experience received (Level %d/%d)!".formatted(additional,
                            level,
                            MAXIMUM_LEVEL_FOR_BONUS));
                }
            }
        }
    }
}
