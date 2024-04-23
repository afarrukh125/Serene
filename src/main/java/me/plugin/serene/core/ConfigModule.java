package me.plugin.serene.core;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import me.plugin.serene.model.SereneConfiguration;
import org.bukkit.configuration.file.FileConfiguration;

public class ConfigModule extends AbstractModule {
    private final FileConfiguration fileConfig;

    public ConfigModule(FileConfiguration fileConfig) {
        this.fileConfig = fileConfig;
    }

    @Provides
    public SereneConfiguration sereneConfiguration() {
        return new SereneConfiguration(fileConfig);
    }
}
