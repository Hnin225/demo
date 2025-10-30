package com.example.demo.repository;

import com.example.demo.entity.NoticeAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NoticeAttachmentRepository extends JpaRepository<NoticeAttachment, Long> {

    List<NoticeAttachment> findByNoticeId(Long noticeId);

    void deleteByNoticeId(Long noticeId);
}