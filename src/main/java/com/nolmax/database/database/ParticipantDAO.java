package com.nolmax.database.database;

import com.nolmax.database.config.DatabaseConfig;
import com.nolmax.database.model.Participant;
import com.nolmax.database.util.IdGenerator;

import java.sql.*;
import java.util.ArrayList;

public class ParticipantDAO {

    private boolean joinNew(Participant participant) {
        String sql = "INSERT INTO participants (conversation_id, user_id, update_id) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConfig.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, participant.getConversationId());
            stmt.setLong(2, participant.getUserId());
            stmt.setLong(3, IdGenerator.getInstance().nextId());
            int rows = stmt.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean joinAgain(Participant participant) {
        String sql = "UPDATE participants SET left_at = NULL, update_id = ? WHERE conversation_id = ? AND user_id = ?";
        try (Connection conn = DatabaseConfig.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, IdGenerator.getInstance().nextId());
            stmt.setLong(2, participant.getConversationId());
            stmt.setLong(3, participant.getUserId());
            int rows = stmt.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean join(Participant participant) {
        String sql = "SELECT * FROM participants WHERE conversation_id = ? AND user_id = ?";
        try (Connection conn = DatabaseConfig.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, participant.getConversationId());
            stmt.setLong(2, participant.getUserId());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Timestamp leftAt = rs.getTimestamp("left_at");
                    if (leftAt != null) {
                        return joinAgain(participant);
                    } else {
                        // Đã ở trong nhóm, không cần làm gì thêm
                        return true;
                    }
                } else {
                    // Chưa từng tham gia nhóm
                    return joinNew(participant);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean left(Long conversationId, Long userId) {
        String sql = "UPDATE participants SET left_at = CURRENT_TIMESTAMP, update_id = ? WHERE conversation_id = ? AND user_id = ?";
        try (Connection conn = DatabaseConfig.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, IdGenerator.getInstance().nextId());
            stmt.setLong(2, conversationId);
            stmt.setLong(3, userId);
            int rows = stmt.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean updateRole(Long conversationId, Long userId, int role) {
        String sql = "UPDATE participants SET role = ?, update_id = ? WHERE conversation_id = ? AND user_id = ?";
        try (Connection conn = DatabaseConfig.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, role);
            stmt.setLong(2, IdGenerator.getInstance().nextId());
            stmt.setLong(3, conversationId);
            stmt.setLong(4, userId);
            int rows = stmt.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean updateLastReadMessageId(Long conversationId, Long userId, Long messageId) {
        String sql = "UPDATE participants SET last_read_message_id = ?, update_id = ? WHERE conversation_id = ? AND user_id = ?";
        try (Connection conn = DatabaseConfig.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, messageId);
            stmt.setLong(2, IdGenerator.getInstance().nextId());
            stmt.setLong(3, conversationId);
            stmt.setLong(4, userId);
            int rows = stmt.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public ArrayList<Participant> pull(Long conversationId, Long lastUpdateId) {
        String sql = "SELECT * FROM participants WHERE conversation_id = ? AND update_id > ? ORDER BY update_id ASC";
        ArrayList<Participant> participants = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, conversationId);
            stmt.setLong(2, lastUpdateId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Participant participant = new Participant();
                    participant.setConversationId(rs.getLong("conversation_id"));
                    participant.setUserId(rs.getLong("user_id"));
                    participant.setRole(rs.getInt("role"));
                    participant.setJoinedAt(rs.getTimestamp("joined_at") != null ? rs.getTimestamp("joined_at").toLocalDateTime() : null);
                    participant.setLeftAt(rs.getTimestamp("left_at") != null ? rs.getTimestamp("left_at").toLocalDateTime() : null);
                    participant.setLastReadMessageId(rs.getLong("last_read_message_id"));
                    participant.setUpdateId(rs.getLong("update_id"));
                    participants.add(participant);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
    }
        return participants;
    }
}
