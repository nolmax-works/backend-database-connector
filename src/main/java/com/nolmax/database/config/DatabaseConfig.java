package com.nolmax.database.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;

public class DatabaseConfig {
    private static HikariDataSource dataSource;

    private DatabaseConfig() {
        // utility class
    }

    public static synchronized void initialize(String jdbcUrl, String username, String password) {
        if (dataSource != null && !dataSource.isClosed()) {
            return;
        }

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(jdbcUrl);
        config.setUsername(username);
        config.setPassword(password);
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setIdleTimeout(30000);
        config.setConnectionTimeout(30000);
        config.setMaxLifetime(1800000);

        dataSource = new HikariDataSource(config);
    }

    public static synchronized void initialize(String ip, String port, String databaseName, String username, String password) {
        String jdbcUrl = "jdbc:postgresql://" + ip + ":" + port + "/" + databaseName;
        initialize(jdbcUrl, username, password);
    }

    public static DataSource getDataSource() {
        if (dataSource == null) {
            throw new IllegalStateException("DatabaseConfig is not initialized. Call initialize(...) first.");
        }
        return dataSource;
    }

    public static synchronized void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            dataSource = null;
        }
    }
}
