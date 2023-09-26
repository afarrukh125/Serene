package me.plugin.serene.model;

import org.bukkit.configuration.file.FileConfiguration;

import static java.util.Objects.requireNonNull;

public record SereneConfiguration(FileConfiguration fileConfig) {
    public boolean shouldShowExperienceMessage() {
        return requireNonNull(fileConfig.get("experience.showmessage")).equals("enabled");
    }

    public boolean isBonusExperienceEnabled() {
        return isFeatureEnabled("experience");
    }

    public boolean isInventorySortEnabled() {
        return isFeatureEnabled("inventorysort");
    }

    public boolean is1pSleepEnabled() {
        return isFeatureEnabled("1psleep");
    }

    public boolean isVeinBreakerEnabled() {
        return isFeatureEnabled("veinbreaker");
    }

    public boolean isTreeBreakerEnabled() {
        return isFeatureEnabled("treebreaker");
    }

    private boolean isFeatureEnabled(String ymlPath) {
        return requireNonNull(fileConfig.get("features." + ymlPath)).equals("enabled");
    }
}
