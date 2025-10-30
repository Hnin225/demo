package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "award_detail")
public class AwardDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 일단 관계 주석처리
    // @ManyToOne(fetch = FetchType.LAZY)
    // @JoinColumn(name = "award_id", nullable = false)
    // private Award award;

    @Column(name = "award_id")
    private Long awardId;  // 임시로 Long으로 변경

    @Column(name = "award_title", nullable = false, length = 500)
    private String awardTitle;

    @Column(name = "award_file_name", length = 500)
    private String awardFileName;

    @Column(name = "award_file_path", length = 500)
    private String awardFilePath;

    @Column(name = "upload_date", length = 20)
    private String uploadDate;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}