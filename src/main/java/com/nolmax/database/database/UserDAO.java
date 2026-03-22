package com.nolmax.database.database;
import com.nolmax.database.config.DatabaseConfig;
import com.nolmax.database.model.User;
import com.nolmax.database.util.IdGenerator;

import java.sql.*;
import java.util.ArrayList;

public class UserDAO {
    public boolean register(User user) {
        String sql = "INSERT INTO users (username, password_hash, avatar_url, update_id) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getPasswordHash());
            stmt.setString(3, user.getAvatarUrl());
            stmt.setLong(4, IdGenerator.getInstance().nextId());

            int rows = stmt.executeUpdate();
            if (rows > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        user.setId(rs.getLong(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean login(User user) {
        String sql = "SELECT * FROM users WHERE username = ? AND password_hash = ?";
        try (Connection conn = DatabaseConfig.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getPasswordHash());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    user.setId(rs.getLong("id"));
                    user.setAvatarUrl(rs.getString("avatar_url"));
                    user.setUpdateId(rs.getLong("update_id"));
                    return true;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean updateAvatar(Long id, String avatarUrl) {
        String sql = "UPDATE users SET avatar_url = ?, update_id = ? WHERE id = ?";
        try (Connection conn = DatabaseConfig.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, avatarUrl);
            stmt.setLong(2, IdGenerator.getInstance().nextId());
            stmt.setLong(3, id);

            int rows = stmt.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean updatePassword(Long id, String passwordHash) {
        String sql = "UPDATE users SET password_hash = ?, update_id = ? WHERE id = ?";
        try (Connection conn = DatabaseConfig.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, passwordHash);
            stmt.setLong(2, IdGenerator.getInstance().nextId());
            stmt.setLong(3, id);

            int rows = stmt.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public ArrayList<User> pull(Long conversationId, Long lastUpdateId) {
        ArrayList<User> users = new ArrayList<>();
        String sql = "SELECT u.id, u.username, u.avatar_url, u.update_id " +
                     "FROM users u " +
                     "JOIN conversation_participants cp ON u.id = cp.user_id " +
                     "WHERE cp.conversation_id = ? AND u.update_id > ?";
        try (Connection conn = DatabaseConfig.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
             stmt.setLong(1, conversationId);
             stmt.setLong(2, lastUpdateId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    User user = new User();
                    user.setId(rs.getLong("id"));
                    user.setUsername(rs.getString("username"));
                    user.setAvatarUrl(rs.getString("avatar_url"));
                    user.setUpdateId(rs.getLong("update_id"));
                    users.add(user);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }
}
