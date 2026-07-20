package com.example.jobfinderapp.models;

public class Notification {
    private int id;
    private int userId;
    private String title;
    private String content;
    private String createdAt;
    private int isRead; // 0: chưa đọc, 1: đã đọc

    public Notification() {}

    public Notification(int id, int userId, String title, String content, String createdAt, int isRead) {
        this.id = id;
        this.userId = userId;
        this.title = title;
        this.content = content;
        this.createdAt = createdAt;
        this.isRead = isRead;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public int getIsRead() { return isRead; }
    public void setIsRead(int isRead) { this.isRead = isRead; }
}
