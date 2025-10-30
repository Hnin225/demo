package com.example.demo.repository;

import com.example.demo.entity.Visit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VisitRepository extends JpaRepository<Visit, Long> {

    @Query("SELECT v FROM Visit v ORDER BY v.pinned DESC, v.createdAt DESC")
    List<Visit> findAllOrderByPinnedAndCreatedAt();

    List<Visit> findByStatusOrderByPinnedDescCreatedAtDesc(String status);
}