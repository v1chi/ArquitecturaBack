package com.team.socialnetwork.repository;

import com.team.socialnetwork.entity.CommentLike;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentLikeRepository extends JpaRepository<CommentLike, Long> {
    long countByCommentId(Long commentId);
    Page<CommentLike> findByCommentId(Long commentId, Pageable pageable);
    boolean existsByUserIdAndCommentId(Long userId, Long commentId);
    void deleteByUserIdAndCommentId(Long userId, Long commentId);
    void deleteByCommentId(Long commentId);
    void deleteByUserId(Long userId);
}

