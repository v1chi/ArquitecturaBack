package com.team.socialnetwork.repository;

import com.team.socialnetwork.entity.CommentLike;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface CommentLikeRepository extends JpaRepository<CommentLike, Long> {
    long countByCommentId(Long commentId);
    Page<CommentLike> findByCommentId(Long commentId, Pageable pageable);
    boolean existsByUserIdAndCommentId(Long userId, Long commentId);

    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from CommentLike cl where cl.user.id = :userId and cl.comment.id = :commentId")
    int deleteByUserIdAndCommentId(@Param("userId") Long userId, @Param("commentId") Long commentId);
    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from CommentLike cl where cl.comment.id = :commentId")
    int deleteByCommentId(@Param("commentId") Long commentId);
    void deleteByUserId(Long userId);
}
