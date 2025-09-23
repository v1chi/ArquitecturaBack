package com.team.socialnetwork.repository;

import com.team.socialnetwork.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findByAuthorId(Long authorId);
    Page<Post> findByAuthorIdIn(Collection<Long> authorIds, Pageable pageable);
}
