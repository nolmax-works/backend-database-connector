package com.nolmax.database.database;
import com.nolmax.database.config.DatabaseConfig;
import com.nolmax.database.model.Conversation;
import com.nolmax.database.util.IdGenerator;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ConversationDAO {
    public boolean createConversation(Conversation conversation) {
        String sql = "INSERT INTO conversations (type, name, avatar_url, created_by, update_id) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1,conversation.getType());
            stmt.setString(2,conversation.getName());
            stmt.setString(3,conversation.getAvatarUrl());
            stmt.setLong(4,conversation.getCreatedBy());
            stmt.setLong(5, IdGenerator.getInstance().nextId());

            int rows = stmt.executeUpdate();
            if (rows > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        conversation.setId(rs.getLong(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean updateAvatar(Long id, String avatarUrl) {
        String sql = "UPDATE conversations SET avatar_url = ?, update_id = ? WHERE id = ?";
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

    public boolean updateName(Long id, String name) {
        String sql = "UPDATE conversations SET name = ?, update_id = ? WHERE id = ?";
        try (Connection conn = DatabaseConfig.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, name);
            stmt.setLong(2, IdGenerator.getInstance().nextId());
            stmt.setLong(3, id);

            int rows = stmt.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean updateLastMessageId(Long id, Long messageId) {
        String sql = "UPDATE conversations SET last_message_id = ?, update_id = ? WHERE id = ?";
        try (Connection conn = DatabaseConfig.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, messageId);
            stmt.setLong(2, IdGenerator.getInstance().nextId());
            stmt.setLong(3, id);

            int rows = stmt.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public  boolean deleteConversation(Long id) {
        String sql = "DELETE FROM conversations WHERE id = ?";
        try (Connection conn = DatabaseConfig.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);

            int rows = stmt.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<Conversation> pull(Long lastUpdateId, Long userId) {
        String sql = "SELECT c.id, c.type, c.name, c.avatar_url, c.update_id, c.last_message_id " +
                     "FROM conversations c " +
                     "JOIN conversation_participants cp ON c.id = cp.conversation_id " +
                     "WHERE cp.user_id = ? AND c.update_id > ?";
        List<Conversation> conversations = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
             stmt.setLong(1, userId);
             stmt.setLong(2, lastUpdateId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Conversation conversation = new Conversation();
                    conversation.setId(rs.getLong("id"));
                    conversation.setType(rs.getInt("type"));
                    conversation.setName(rs.getString("name"));
                    conversation.setAvatarUrl(rs.getString("avatar_url"));
                    conversation.setUpdateId(rs.getLong("update_id"));
                    conversation.setLastMessageId(rs.getLong("last_message_id"));
                    conversations.add(conversation);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return conversations;
    }
}
