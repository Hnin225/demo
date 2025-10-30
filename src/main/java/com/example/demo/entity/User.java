package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 50)
    private String userId;  // 아이디

    @Column(nullable = false, length = 255)
    private String password;  // 비밀번호

    @Column(nullable = false, length = 100)
    private String name;  // 이름

    @Column(nullable = false, length = 100)
    private String email;  // 이메일

    @Column(length = 20)
    private String phone;  // 연락처

    @Column(length = 100)
    private String department;  // 소속 부서

    @Column(name = "user_group", length = 50)
    private String group;  // 그룹 (No, 그룹 이름, 전력, 선박)

    @Column(name = "account_status", length = 20)
    private String accountStatus;  // 계정 상태 (사용, 미사용)

    @Column(name = "last_login")
    private LocalDateTime lastLogin;  // 마지막 접속 일시

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (accountStatus == null) {
            accountStatus = "사용";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}