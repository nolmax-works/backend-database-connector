package com.nolmax.database.model;

import java.sql.Timestamp;
import java.time.LocalDateTime;

public class User {
    private Long id;
    private String username;
    private String passwordHash;
    private String avatarUrl;
    private Long updateId;


    public User() {}

    public User(Long id, String username, String passwordHash, String avatarUrl, Long updateId) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.avatarUrl = avatarUrl;
        this.updateId = updateId;
    }

    // Getter & Setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }

    public Long getUpdateId() { return updateId; }
    public void setUpdateId(Long updateId) { this.updateId = updateId; }
}
