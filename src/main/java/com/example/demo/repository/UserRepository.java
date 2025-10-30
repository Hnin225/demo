package com.example.demo.repository;

import com.example.demo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUserId(String userId);

    boolean existsByUserId(String userId);

    List<User> findAllByOrderByCreatedAtDesc();

    List<User> findByUserIdContainingOrNameContainingOrEmailContaining(
            String userId, String name, String email);
}