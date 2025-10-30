package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "notice")
public class Notice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title", nullable = false, length = 200)
    private String title;  // 제목

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;  // 내용

    @Column(name = "category", length = 50)
    private String category;  // 분류 (안전, 교통, 환경 등)

    @Column(name = "author", length = 100)
    private String author;  // 작성자

    @Column(name = "status", length = 20)
    private String status = "게시 중";  // 상태: 게시 중, 예약, 게시 종료

    @Column(name = "pinned", nullable = false)
    private Boolean pinned = false;  // 고정 여부

    @Column(name = "view_count", nullable = false)
    private Integer viewCount = 0;  // 조회수

    @Column(name = "start_date")
    private LocalDateTime startDate;  // 게시 시작일

    @Column(name = "end_date")
    private LocalDateTime endDate;  // 게시 종료일

    @Column(name = "created_at")
    private LocalDateTime createdAt;  // 작성일시

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;  // 수정일시

    @Column(name = "attachment_count", nullable = false)
    private Integer attachmentCount = 0;  // 첨부파일 개수

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (pinned == null) {
            pinned = false;
        }
        if (viewCount == null) {
            viewCount = 0;
        }
        if (attachmentCount == null) {
            attachmentCount = 0;
        }
        if (status == null || status.trim().isEmpty()) {
            status = "게시 중";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}