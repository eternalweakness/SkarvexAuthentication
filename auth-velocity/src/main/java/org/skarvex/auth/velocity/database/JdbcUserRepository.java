package org.skarvex.auth.velocity.database;

import org.skarvex.auth.core.model.User;
import org.skarvex.auth.core.repository.UserRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

public class JdbcUserRepository
        implements UserRepository {

    private final DatabaseManager db;
    public JdbcUserRepository(DatabaseManager db) {
        this.db = db;
    }

    @Override
    public Optional<User> findById(UUID uuid) {

        String sql = "SELECT * FROM users WHERE uuid = ?";

        try (Connection connection = db.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setString(1, uuid.toString());

            ResultSet resultSet = statement.executeQuery();

            if (!resultSet.next())
                return Optional.empty();

            return Optional.of(
                    new User(
                            UUID.fromString(resultSet.getString("uuid")),
                            resultSet.getString("name"),
                            resultSet.getString("password_hash")
                    )
            );

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void save(User user) {
        String sql = "INSERT INTO users(uuid, name, password_hash) VALUES(?,?,?)";

        try (Connection connection = db.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setString(1, user.id().toString());
            statement.setString(2, user.name());
            statement.setString(3, user.passwordHash());

            statement.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean updatePassword(UUID uuid, String passwordHash) {
        String sql = "UPDATE users SET password_hash = ? WHERE uuid = ?";

        try (Connection connection = db.getConnection();
        PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, passwordHash);
            statement.setString(2, uuid.toString());

            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
