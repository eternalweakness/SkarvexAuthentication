package org.skarvex.auth.velocity.database;

import org.skarvex.auth.core.model.RestrictedIp;
import org.skarvex.auth.core.repository.RestrictedIpRepository;

import java.sql.*;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class JdbcRestrictedIpRepository implements RestrictedIpRepository {

    private final DatabaseManager db;

    public JdbcRestrictedIpRepository(DatabaseManager db) {
        this.db = db;
    }

    @Override
    public CompletableFuture<Void> block(RestrictedIp restrictedIp) {
        return CompletableFuture.runAsync(() -> {
            String sql = "INSERT INTO restricted_ips(ip, blocked_until) VALUES(?,?)";

            try (Connection connection = db.getConnection();
                 PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, restrictedIp.ip());
                statement.setTimestamp(2, Timestamp.from(restrictedIp.blockedUntil()));

                statement.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public CompletableFuture<Optional<RestrictedIp>> findByIp(String ip) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = "SELECT ip, blocked_until FROM restricted_ips WHERE ip = ?";

            try (Connection connection = db.getConnection();
                 PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, ip);

                ResultSet resultSet = statement.executeQuery();

                if (!resultSet.next()) return Optional.empty();

                return Optional.of(new RestrictedIp(
                        resultSet.getString("ip"),
                        resultSet.getTimestamp("blocked_until").toInstant()
                ));
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public CompletableFuture<Void> unblock(String ip) {
        return CompletableFuture.runAsync(() -> {
            String sql = "DELETE FROM restricted_ips WHERE ip = ?";

           try (Connection connection = db.getConnection();
           PreparedStatement statement = connection.prepareStatement(sql)) {

               statement.setString(1, ip);
               statement.executeUpdate();

           } catch (SQLException e) {
               throw new RuntimeException(e);
           }
        });
    }
}
