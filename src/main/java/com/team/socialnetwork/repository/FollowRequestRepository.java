package com.team.socialnetwork.repository;

import com.team.socialnetwork.entity.FollowRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FollowRequestRepository extends JpaRepository<FollowRequest, Long> {
    boolean existsByFollowerIdAndTargetId(Long followerId, Long targetId);
    Optional<FollowRequest> findByFollowerIdAndTargetId(Long followerId, Long targetId);
    List<FollowRequest> findByTargetId(Long targetId);
}

