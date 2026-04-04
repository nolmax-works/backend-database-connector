package com.nolmax.database.database;
import com.nolmax.database.model.Message;
import com.nolmax.database.util.IdGenerator;

import java.util.ArrayList;

public class MessageDAO {
    public boolean createMessage(Message message) {
        String sql = "INSERT INTO messages (id, conversation_id, sender_id, content) " +
                     "VALUES (?, ?, ?, ?)";
        try (var conn = com.nolmax.database.config.DatabaseConfig.getDataSource().getConnection();
             var stmt = conn.prepareStatement(sql)) {
            message.setId(IdGenerator.getInstance().nextId());
            stmt.setLong(1, message.getId());
            stmt.setLong(2, message.getConversationId());
            stmt.setLong(3, message.getSenderId());
            stmt.setObject(4, message.getContent());
            stmt.executeUpdate();
            ParticipantDAO participantDAO = new ParticipantDAO();
            participantDAO.updateLastReadMessageId(message.getConversationId(), message.getSenderId(), message.getId());
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public ArrayList<Message> pull(Long conversationId, Long lastUpdateId) {
        ArrayList<Message> messages = new ArrayList<>();
        String sql = "SELECT id, conversation_id, sender_id, content, sent_at FROM messages WHERE conversation_id = ? AND id > ? ORDER BY id ASC LIMIT 50";
        try (var conn = com.nolmax.database.config.DatabaseConfig.getDataSource().getConnection();
             var stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, conversationId);
            stmt.setLong(2, lastUpdateId);
            try (var rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Message message = new Message();
                    message.setId(rs.getLong("id"));
                    message.setConversationId(rs.getLong("conversation_id"));
                    message.setSenderId(rs.getLong("sender_id"));
                    message.setContent(rs.getString("content"));
                    message.setSentAt(rs.getTimestamp("sent_at").toLocalDateTime());
                    messages.add(message);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return messages;
    }
}
