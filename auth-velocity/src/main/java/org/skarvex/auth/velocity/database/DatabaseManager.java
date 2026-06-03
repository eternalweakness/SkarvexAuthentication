package org.skarvex.auth.velocity.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.skarvex.auth.velocity.manager.ConfigurationManager;
import org.slf4j.Logger;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {

    private final ConfigurationManager config;
    private final Logger logger;
    private HikariDataSource dataSource;

    public DatabaseManager(ConfigurationManager config, Logger logger) {
        this.config = config;
        this.logger = logger;
    }

    public void connect() {
        HikariConfig hikari = new HikariConfig();

        hikari.setJdbcUrl(config.getString("database.url"));
        hikari.setUsername(config.getString("database.username"));
        hikari.setPassword(config.getString("database.password"));

        hikari.setMaximumPoolSize(10);
        hikari.setMinimumIdle(2);

        logger.info("URL: {}", config.getString("database.url"));
        logger.info("User: {}", config.getString("database.username"));

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            logger.info("MySQL driver loaded");
        } catch (ClassNotFoundException e) {
            logger.error("MySQL driver missing", e);
        }

        this.dataSource = new HikariDataSource(hikari);

        try (
                Connection connection = getConnection();
                Statement statement = connection.createStatement()
        ) {
            statement.execute("""
                CREATE TABLE IF NOT EXISTS users (
                    uuid VARCHAR(36) PRIMARY KEY,
                    name VARCHAR(16) NOT NULL,
                    password_hash VARCHAR(255) NOT NULL
                )
            """);

            logger.info("[HikariCP] Database connected");
        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize database: ", e);
        }
    }

    public void disconnect() {
        if (dataSource != null) {
            dataSource.close();
            logger.info("[HikariCP] Database disconnected");
        }
    }

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public HikariDataSource getDataSource() {
        return dataSource;
    }

}
