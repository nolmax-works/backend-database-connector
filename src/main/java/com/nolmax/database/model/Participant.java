package com.nolmax.database.model;
import java.time.LocalDateTime;

public class Participant {
    private Long conversationId;
    private Long userId;
    private Integer role;  // 0 for member, 1 for admin
    private LocalDateTime joinedAt;
    private LocalDateTime leftAt;
    private Long lastReadMessageId;
    private Long updateId;

    public Participant() {
    }

    public Participant(Long conversationId, Long userId, Integer role, LocalDateTime joinedAt, LocalDateTime leftAt, Long lastReadMessageId, Long updateId) {
        this.conversationId = conversationId;
        this.userId = userId;
        this.role = role;
        this.joinedAt = joinedAt;
        this.leftAt = leftAt;
        this.lastReadMessageId = lastReadMessageId;
        this.updateId = updateId;
    }

    public Long getConversationId() { return conversationId; }
    public void setConversationId(Long conversationId) { this.conversationId = conversationId;}

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Integer getRole() { return role; }
    public void setRole(Integer role) { this.role = role; }

    public LocalDateTime getJoinedAt() { return joinedAt; }
    public void setJoinedAt(LocalDateTime joinedAt) { this.joinedAt = joinedAt; }

    public LocalDateTime getLeftAt() { return leftAt; }
    public void setLeftAt(LocalDateTime leftAt) { this.leftAt = leftAt; }

    public Long getLastReadMessageId() { return lastReadMessageId; }
    public void setLastReadMessageId(Long lastReadMessageId) { this.lastReadMessageId = lastReadMessageId; }

    public Long getUpdateId() { return updateId; }
    public void setUpdateId(Long updateId) { this.updateId = updateId; }
}
