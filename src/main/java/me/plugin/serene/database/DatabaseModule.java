package me.plugin.serene.database;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatabaseModule extends AbstractModule {
    private static final Logger LOG = LoggerFactory.getLogger(DatabaseModule.class);

    @Provides
    @Singleton
    public SereneDatabaseClient database() {
        try {
            return new SQLiteSereneClient();
        } catch (Exception e) {
            LOG.info(
                    "Could not create connected database client, using default session-based database. {}",
                    e.getMessage());
            return new SessionDatabaseClient();
        }
    }
}
