package com.nolmax.database.database;

import com.nolmax.database.config.DatabaseConfig;
import com.nolmax.database.model.User;
import com.nolmax.database.util.IdGenerator;
import com.nolmax.database.util.PasswordUtils;

import java.security.SecureRandom;
import java.sql.*;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class UserDAO {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final String RANDOM_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    public boolean register(User user) {
        String sql = "INSERT INTO users (username, password_hash, avatar_url, update_id) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getDataSource().getConnection(); PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
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

    private String generateToken(Long userId) {
        String encodedUserId = new String(Base64.getEncoder().encode(userId.toString().getBytes()));
        String encodedTimestamp = new String(Base64.getEncoder().encode(String.valueOf(System.currentTimeMillis() / 1000).getBytes()));
        String randomChar = generateRandomString(10);

        return encodedUserId + "$" + encodedTimestamp + "$" + randomChar;
    }

    private String generateRandomString(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(RANDOM_CHARS.charAt(SECURE_RANDOM.nextInt(RANDOM_CHARS.length())));
        }
        return sb.toString();
    }

    private String createToken(Connection conn, long userId) throws SQLException {
        String token = generateToken(userId);

        String sql = "INSERT INTO user_tokens (token, user_id) VALUES (?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, token);
            stmt.setLong(2, userId);
            stmt.executeUpdate();
            return token;
        }
    }

    public String requestToken(String username, String password) {
        String sql = "SELECT id, password_hash FROM users WHERE username = ?";
        try (Connection conn = DatabaseConfig.getDataSource().getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    long userId = rs.getLong("id");
                    String storedHash = rs.getString("password_hash");

                    if (PasswordUtils.verifyPassword(password, storedHash)) {
                        return createToken(conn, userId);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean loginWithToken(User user) {
        String sql = "SELECT u.id, u.username, u.avatar_url, u.update_id " + "FROM user_tokens t " + "JOIN users u ON u.id = t.user_id " + "WHERE t.token = ?";
        try (Connection conn = DatabaseConfig.getDataSource().getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, user.getToken());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    user.setId(rs.getLong("id"));
                    user.setUsername(rs.getString("username"));
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

    public boolean logout(Long userId, String token) {
        String sql = "DELETE FROM user_tokens WHERE user_id = ? AND token = ?";
        try (Connection conn = DatabaseConfig.getDataSource().getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, userId);
            stmt.setString(2, token);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean updateAvatar(Long id, String avatarUrl) {
        String sql = "UPDATE users SET avatar_url = ?, update_id = ? WHERE id = ?";
        try (Connection conn = DatabaseConfig.getDataSource().getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
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
        try (Connection conn = DatabaseConfig.getDataSource().getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
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
        String sql = "SELECT u.id, u.username, u.avatar_url, u.update_id " + "FROM users u " + "JOIN participants cp ON u.id = cp.user_id " + "WHERE cp.conversation_id = ? AND u.update_id > ?";
        try (Connection conn = DatabaseConfig.getDataSource().getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
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

    public ArrayList<User> pullBatch(List<Long> conversationIds, Long lastUpdateId) {
        if (conversationIds == null || conversationIds.isEmpty()) return new ArrayList<>();
        ArrayList<User> users = new ArrayList<>();

        String inClause = String.join(",", java.util.Collections.nCopies(conversationIds.size(), "?"));
        String sql = "SELECT DISTINCT u.id, u.username, u.avatar_url, u.update_id " +
                "FROM users u " +
                "JOIN participants cp ON u.id = cp.user_id " +
                "WHERE cp.conversation_id IN (" + inClause + ") AND u.update_id > ?";

        try (Connection conn = DatabaseConfig.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            int index = 1;
            for (Long id : conversationIds) {
                stmt.setLong(index++, id);
            }
            stmt.setLong(index, lastUpdateId);

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
