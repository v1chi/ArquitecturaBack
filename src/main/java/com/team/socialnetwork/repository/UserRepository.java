package com.team.socialnetwork.repository;

import com.team.socialnetwork.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByName(String name);
    long deleteByEmailConfirmedFalseAndCreatedAtBefore(java.time.Instant threshold);
}
