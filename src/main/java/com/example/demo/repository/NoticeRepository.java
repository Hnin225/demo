package com.example.demo.repository;

import com.example.demo.entity.Notice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NoticeRepository extends JpaRepository<Notice, Long> {

    // 고정 게시글 + 일반 게시글 (작성일 역순)
    @Query("SELECT n FROM Notice n ORDER BY n.pinned DESC, n.createdAt DESC")
    List<Notice> findAllOrderByPinnedAndCreatedAt();

    // 제목 검색
    @Query("SELECT n FROM Notice n WHERE n.title LIKE %:keyword% ORDER BY n.pinned DESC, n.createdAt DESC")
    List<Notice> findByTitleContaining(@Param("keyword") String keyword);

    // 작성자 검색
    @Query("SELECT n FROM Notice n WHERE n.author LIKE %:keyword% ORDER BY n.pinned DESC, n.createdAt DESC")
    List<Notice> findByAuthorContaining(@Param("keyword") String keyword);

    // 전체 검색 (제목 + 작성자)
    @Query("SELECT n FROM Notice n WHERE n.title LIKE %:keyword% OR n.author LIKE %:keyword% ORDER BY n.pinned DESC, n.createdAt DESC")
    List<Notice> findByKeyword(@Param("keyword") String keyword);

    // 상태별 검색
    List<Notice> findByStatusOrderByPinnedDescCreatedAtDesc(String status);
}