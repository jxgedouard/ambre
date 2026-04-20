package com.ambre.model;

import java.time.LocalDateTime;

public class User {

    private String id;
    private String username;
    private String passwordHash; // hashé avec BCrypt, jamais en clair
    private LocalDateTime createdAt;

    // Constructeur vide requis par Gson
    public User() {}

    public User(String id, String username, String passwordHash, LocalDateTime createdAt) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.createdAt = createdAt;
    }

    public String getId() { return id; }
    public String getUsername() { return username; }
    public String getPasswordHash() { return passwordHash; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setId(String id) { this.id = id; }
    public void setUsername(String username) { this.username = username; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
