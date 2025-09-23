package com.team.socialnetwork.repository;

import com.team.socialnetwork.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
    long deleteByEmailConfirmedFalseAndCreatedAtBefore(java.time.Instant threshold);

    @Query("select u from User u where lower(u.username) like lower(concat('%', :term, '%')) " +
           "or lower(coalesce(u.fullName, '')) like lower(concat('%', :term, '%'))")
    List<User> searchByTerm(@Param("term") String term);
}
