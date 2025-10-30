package com.example.demo.repository;

import com.example.demo.entity.Press;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PressRepository extends JpaRepository<Press, Long> {

    @Query("SELECT p FROM Press p ORDER BY p.pinned DESC, p.createdAt DESC")
    List<Press> findAllOrderByPinnedAndCreatedAt();

    List<Press> findByStatusOrderByPinnedDescCreatedAtDesc(String status);
}