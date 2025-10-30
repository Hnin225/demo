package com.example.demo.repository;

import com.example.demo.entity.VisitAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VisitAttachmentRepository extends JpaRepository<VisitAttachment, Long> {
    List<VisitAttachment> findByVisitId(Long visitId);
    void deleteByVisitId(Long visitId);
}