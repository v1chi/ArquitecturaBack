package com.team.socialnetwork.repository;

import com.team.socialnetwork.entity.Comment;
import com.team.socialnetwork.repository.projection.PostIdCountProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByPostId(Long postId);
    List<Comment> findByAuthorId(Long authorId);

    @Query("select c.post.id as postId, count(c) as cnt from Comment c where c.post.id in :postIds group by c.post.id")
    List<PostIdCountProjection> countByPostIds(@Param("postIds") Collection<Long> postIds);
}
