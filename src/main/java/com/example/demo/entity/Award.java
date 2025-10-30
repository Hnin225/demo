package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "award")
public class Award {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "award_year", nullable = false)
    private Integer year;  // 연도

    @Column(name = "title", nullable = false, length = 50)
    private String title;  // 게시글 타이틀 (10-50자)

    @Column(name = "author", length = 100)
    private String author;  // 작성자

    @Column(name = "image_file_name", length = 500)
    private String imageFileName;  // 이미지 파일 이름

    @Column(name = "image_file_path", length = 500)
    private String imageFilePath;  // 이미지 파일 경로

    @Column(name = "is_visible", nullable = false)
    private Boolean isVisible = true;  // 게시 여부

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (isVisible == null) {
            isVisible = true;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}