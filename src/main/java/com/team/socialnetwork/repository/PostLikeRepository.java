package com.team.socialnetwork.repository;

import com.team.socialnetwork.entity.PostLike;
import com.team.socialnetwork.repository.projection.PostIdCountProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;

public interface PostLikeRepository extends JpaRepository<PostLike, Long> {
    long countByPostId(Long postId);
    boolean existsByUserIdAndPostId(Long userId, Long postId);

    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from PostLike pl where pl.user.id = :userId and pl.post.id = :postId")
    int deleteByUserIdAndPostId(@Param("userId") Long userId, @Param("postId") Long postId);

    Page<PostLike> findByPostId(Long postId, Pageable pageable);

    @Query("select pl.post.id as postId, count(pl) as cnt from PostLike pl where pl.post.id in :postIds group by pl.post.id")
    List<PostIdCountProjection> countByPostIds(@Param("postIds") Collection<Long> postIds);

    @Query("select pl.post.id from PostLike pl where pl.user.id = :userId and pl.post.id in :postIds")
    List<Long> findPostIdsLikedByUser(@Param("userId") Long userId, @Param("postIds") Collection<Long> postIds);
}
