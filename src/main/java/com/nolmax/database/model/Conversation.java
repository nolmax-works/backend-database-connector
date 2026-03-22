package com.nolmax.database.model;

import java.time.LocalDateTime;

public class Conversation {
    private Long id;
    private Integer type; // 0 for private, 1 for group
    private String name;
    private String avatarUrl;
    private Long createdBy;
    private Long updateId;
    private Long lastMessageId;

    public Conversation() {
    }

    public Conversation(Long id ,Integer type, String name, String avatarUrl, Long createdBy, Long updateId, Long lastMessageId) {
        this.id = id;
        this.type = type;
        this.name = name;
        this.avatarUrl = avatarUrl;
        this.createdBy = createdBy;
        this.updateId = updateId;
        this.lastMessageId = lastMessageId;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Integer getType() { return type; }
    public void setType(Integer type) { this.type = type; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }

    public Long getCreatedBy() { return createdBy; }
    public void setCreatedBy(Long createdBy) { this.createdBy = createdBy; }

    public Long getUpdateId() { return updateId; }
    public void setUpdateId(Long updateId) { this.updateId = updateId; }

    public Long getLastMessageId() { return lastMessageId; }
    public void setLastMessageId(Long lastMessageId) { this.lastMessageId = lastMessageId; }

}
