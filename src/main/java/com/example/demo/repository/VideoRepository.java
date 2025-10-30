package com.example.demo.repository;

import com.example.demo.entity.Video;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VideoRepository extends JpaRepository<Video, Long> {

    @Query("SELECT v FROM Video v ORDER BY v.pinned DESC, v.createdAt DESC")
    List<Video> findAllOrderByPinnedAndCreatedAt();

    List<Video> findByStatusOrderByPinnedDescCreatedAtDesc(String status);
}