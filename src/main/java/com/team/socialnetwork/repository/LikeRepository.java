package com.team.socialnetwork.repository;

import com.team.socialnetwork.entity.Like;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LikeRepository extends JpaRepository<Like, Long> {
    long countByPostId(Long postId);
    boolean existsByUserIdAndPostId(Long userId, Long postId);
    void deleteByUserIdAndPostId(Long userId, Long postId);
    Page<Like> findByPostId(Long postId, Pageable pageable);
}
