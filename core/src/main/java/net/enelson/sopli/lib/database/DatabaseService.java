package net.enelson.sopli.lib.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

public final class DatabaseService {

    private final List<SopDatabase> databases = new CopyOnWriteArrayList<SopDatabase>();

    public SopDatabase createDatabase(DatabaseConfig config) {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(config.getJdbcUrl());
        hikariConfig.setUsername(config.getUsername());
        hikariConfig.setPassword(config.getPassword());
        hikariConfig.setPoolName(config.getPoolName());
        hikariConfig.setMaximumPoolSize(config.getMaximumPoolSize());
        hikariConfig.setMinimumIdle(config.getMinimumIdle());
        hikariConfig.setConnectionTimeout(config.getConnectionTimeout());
        hikariConfig.setIdleTimeout(config.getIdleTimeout());
        hikariConfig.setMaxLifetime(config.getMaxLifetime());

        for (Map.Entry<String, String> entry : config.getDataSourceProperties().entrySet()) {
            hikariConfig.addDataSourceProperty(entry.getKey(), entry.getValue());
        }

        final HikariDataSource dataSource = new HikariDataSource(hikariConfig);
        final SopDatabase[] holder = new SopDatabase[1];
        SopDatabase database = new SopDatabase(dataSource, new Runnable() {
            @Override
            public void run() {
                if (holder[0] != null) {
                    databases.remove(holder[0]);
                }
            }
        });
        holder[0] = database;
        databases.add(database);
        return database;
    }

    public void shutdown() {
        for (SopDatabase database : databases) {
            try {
                database.close();
            } catch (Exception ignored) {
            }
        }
        databases.clear();
    }

    @Override
    public String toString() {
        return "DatabaseService{databases=" + databases.size() + '}';
    }
}
