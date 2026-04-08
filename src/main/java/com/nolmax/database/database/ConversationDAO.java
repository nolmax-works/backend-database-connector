package com.nolmax.database.database;

import com.nolmax.database.config.DatabaseConfig;
import com.nolmax.database.model.Conversation;
import com.nolmax.database.model.Participant;
import com.nolmax.database.util.IdGenerator;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ConversationDAO {
    public boolean createConversation(Conversation conversation) {
        String sql = "INSERT INTO conversations (type, name, avatar_url, created_by, update_id) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getDataSource().getConnection(); PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, conversation.getType());
            stmt.setString(2, conversation.getName());
            stmt.setString(3, conversation.getAvatarUrl());
            stmt.setLong(4, conversation.getCreatedBy());
            long newUpdateId = IdGenerator.getInstance().nextId();
            conversation.setUpdateId(newUpdateId);
            stmt.setLong(5, newUpdateId);

            int rows = stmt.executeUpdate();
            if (rows > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        conversation.setId(rs.getLong(1));
                    }
                }

                if (conversation.getType() != null && conversation.getType() == 1) {
                    Participant participant = new Participant();
                    participant.setConversationId(conversation.getId());
                    participant.setUserId(conversation.getCreatedBy());
                    participant.setRole(1);

                    ParticipantDAO participantDAO = new ParticipantDAO();
                    if (!participantDAO.join(participant)) {
                        return false;
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

    public boolean updateName(Long id, String name) {
        String sql = "UPDATE conversations SET name = ?, update_id = ? WHERE id = ?";
        try (Connection conn = DatabaseConfig.getDataSource().getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
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
        try (Connection conn = DatabaseConfig.getDataSource().getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
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

    public boolean deleteConversation(Long id) {
        String sql = "DELETE FROM conversations WHERE id = ?";
        try (Connection conn = DatabaseConfig.getDataSource().getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);

            int rows = stmt.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<Long> takeUserConversations(Long userId) {
        String sql = "SELECT c.id FROM conversations c JOIN participants cp ON c.id = cp.conversation_id WHERE cp.user_id = ?";
        List<Long> conversationIds = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getDataSource().getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    conversationIds.add(rs.getLong("id"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return conversationIds;
    }

    public List<Long> getUserIdList(Long conversationId) {
        String sql = "SELECT user_id FROM participants WHERE conversation_id = ?";
        List<Long> userIds = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getDataSource().getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, conversationId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    userIds.add(rs.getLong("user_id"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return userIds;
    }

    public List<Conversation> pull(Long conversationId, Long lastUpdateId) {
        String sql = "SELECT type, name, avatar_url, update_id, last_message_id, created_by FROM conversations WHERE id = ? AND update_id > ?";
        List<Conversation> conversations = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getDataSource().getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, conversationId);
            stmt.setLong(2, lastUpdateId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Conversation conversation = new Conversation();
                    conversation.setId(conversationId);
                    conversation.setType(rs.getInt("type"));
                    conversation.setName(rs.getString("name"));
                    conversation.setAvatarUrl(rs.getString("avatar_url"));
                    conversation.setUpdateId(rs.getLong("update_id"));
                    conversation.setLastMessageId(rs.getLong("last_message_id"));
                    conversation.setCreatedBy(rs.getLong("created_by"));
                    conversations.add(conversation);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return conversations;
    }

    public boolean isCreator(Long conversationId, Long userId) {
        String sql = "SELECT created_by FROM conversations WHERE id = ?";
        try (Connection conn = DatabaseConfig.getDataSource().getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, conversationId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong("created_by") == userId;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<Conversation> pullBatch(List<Long> conversationIds, Long lastUpdateId) {
        if (conversationIds == null || conversationIds.isEmpty()) return new ArrayList<>();
        List<Conversation> conversations = new ArrayList<>();

        String inClause = String.join(",", java.util.Collections.nCopies(conversationIds.size(), "?"));
        String sql = "SELECT id, type, name, avatar_url, update_id, last_message_id, created_by FROM conversations WHERE id IN (" + inClause + ") AND update_id > ?";

        try (Connection conn = DatabaseConfig.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            int index = 1;
            for (Long id : conversationIds) {
                stmt.setLong(index++, id);
            }
            stmt.setLong(index, lastUpdateId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Conversation conversation = new Conversation();
                    conversation.setId(rs.getLong("id"));
                    conversation.setType(rs.getInt("type"));
                    conversation.setName(rs.getString("name"));
                    conversation.setAvatarUrl(rs.getString("avatar_url"));
                    conversation.setUpdateId(rs.getLong("update_id"));
                    conversation.setLastMessageId(rs.getLong("last_message_id"));
                    conversation.setCreatedBy(rs.getLong("created_by"));
                    conversations.add(conversation);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return conversations;
    }
}
