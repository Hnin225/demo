package com.example.demo.repository;

import com.example.demo.entity.PressAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PressAttachmentRepository extends JpaRepository<PressAttachment, Long> {
    List<PressAttachment> findByPressId(Long pressId);
    void deleteByPressId(Long pressId);
}