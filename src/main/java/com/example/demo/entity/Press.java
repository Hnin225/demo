package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "press")
public class Press {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @Column(name = "category", length = 50)
    private String category;

    @Column(name = "author", length = 100)
    private String author;

    @Column(name = "status", length = 20)
    private String status = "게시 중";

    @Column(name = "pinned", nullable = false)
    private Boolean pinned = false;

    @Column(name = "view_count", nullable = false)
    private Integer viewCount = 0;

    @Column(name = "start_date")
    private LocalDateTime startDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "attachment_count", nullable = false)
    private Integer attachmentCount = 0;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (pinned == null) pinned = false;
        if (viewCount == null) viewCount = 0;
        if (attachmentCount == null) attachmentCount = 0;
        if (status == null || status.trim().isEmpty()) status = "게시 중";
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}