package com.example.demo.entity;

import jakarta.persistence.*;

@Entity
public class FileAttachment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String originalFileName;  // 원본 파일명
    private String savedFileName;     // 서버에 저장된 파일명
    private String filePath;          // 파일 경로
    private Long fileSize;            // 파일 크기

    @ManyToOne
    @JoinColumn(name = "notice_id")
    private Notice notice;

    // Getter, Setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getOriginalFileName() { return originalFileName; }
    public void setOriginalFileName(String originalFileName) {
        this.originalFileName = originalFileName;
    }

    public String getSavedFileName() { return savedFileName; }
    public void setSavedFileName(String savedFileName) {
        this.savedFileName = savedFileName;
    }

    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }

    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }

    public Notice getNotice() { return notice; }
    public void setNotice(Notice notice) { this.notice = notice; }
}