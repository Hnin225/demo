package com.example.demo.repository;

import com.example.demo.entity.VideoAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VideoAttachmentRepository extends JpaRepository<VideoAttachment, Long> {
    List<VideoAttachment> findByVideoId(Long videoId);
    void deleteByVideoId(Long videoId);
}