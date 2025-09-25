package com.team.socialnetwork.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.team.socialnetwork.dto.ChangeNameRequest;
import com.team.socialnetwork.dto.ChangePasswordRequest;
import com.team.socialnetwork.dto.ChangeUsernameRequest;
import com.team.socialnetwork.dto.FollowRequestActionRequest;
import com.team.socialnetwork.dto.FollowRequestResponse;
import com.team.socialnetwork.dto.PostResponse;
import com.team.socialnetwork.dto.PublicUserResponse;
import com.team.socialnetwork.dto.RelationshipResponse;
import com.team.socialnetwork.dto.SafeUser;
import com.team.socialnetwork.dto.UpdateProfileRequest;
import com.team.socialnetwork.dto.UpdateVisibilityRequest;
import com.team.socialnetwork.entity.FollowRequest;
import com.team.socialnetwork.entity.Post;
import com.team.socialnetwork.entity.User;
import com.team.socialnetwork.repository.CommentLikeRepository;
import com.team.socialnetwork.repository.CommentRepository;
import com.team.socialnetwork.repository.FollowRequestRepository;
import com.team.socialnetwork.repository.PostRepository;
import com.team.socialnetwork.repository.UserRepository;
import com.team.socialnetwork.service.NotificationService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/users")
public class UsersController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PostRepository postRepository;
    private final CommentLikeRepository commentLikeRepository;
    private final FollowRequestRepository followRequestRepository;
    private final NotificationService notificationService;

    public UsersController(UserRepository userRepository, PasswordEncoder passwordEncoder,
                           PostRepository postRepository, CommentRepository commentRepository,
                           CommentLikeRepository commentLikeRepository,
                           FollowRequestRepository followRequestRepository,
                           NotificationService notificationService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.postRepository = postRepository;
        this.commentLikeRepository = commentLikeRepository;
        this.followRequestRepository = followRequestRepository;
        this.notificationService = notificationService;
    }

    // Update my visibility (public/private)
    @PatchMapping("/me/visibility")
    public ResponseEntity<com.team.socialnetwork.dto.MessageResponse> updateVisibility(
            Authentication authentication,
            @RequestBody UpdateVisibilityRequest request
    ) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.UNAUTHORIZED, "Missing or invalid token");
        }
        if (request.getIsPrivate() == null) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST, "isPrivate is required");
        }
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND, "User not found"));
        boolean requestedPrivate = Boolean.TRUE.equals(request.getIsPrivate());
        if (user.isPrivate() == requestedPrivate) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST,
                    "Visibility is already set to " + (requestedPrivate ? "private" : "public")
            );
        }
        user.setPrivate(requestedPrivate);
        userRepository.save(user);
        return ResponseEntity.ok(new com.team.socialnetwork.dto.MessageResponse("Visibility updated"));
    }

    @GetMapping("/me")
    public ResponseEntity<PublicUserResponse> me(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.UNAUTHORIZED, "Missing or invalid token");
        }
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND, "User not found"));
        long followersCount = user.getFollowers().size();
        long followingCount = user.getFollowing().size();
        PublicUserResponse dto = new PublicUserResponse(
                user.getId(), user.getFullName(), user.getUsername(), user.getEmail(), user.getCreatedAt(),
                followersCount, followingCount, user.isPrivate()
        );
        return ResponseEntity.ok(dto);
    }

    // Follow a user
    @PostMapping("/{userId}/follow")
    public ResponseEntity<com.team.socialnetwork.dto.MessageResponse> follow(Authentication authentication,
                                                                             @PathVariable Long userId) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.UNAUTHORIZED, "Missing or invalid token");
        }
        String email = authentication.getName();
        User me = userRepository.findByEmail(email)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND, "User not found"));
        if (me.getId().equals(userId)) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST, "You cannot follow yourself");
        }
        User target = userRepository.findById(userId)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND, "User not found"));

        if (me.getFollowing().contains(target)) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.CONFLICT, "Already following");
        }
        if (target.isPrivate()) {
            if (followRequestRepository.existsByFollowerIdAndTargetId(me.getId(), target.getId())) {
                throw new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.CONFLICT, "Follow request already sent");
            }
            FollowRequest followRequest = new FollowRequest(me, target);
            followRequestRepository.save(followRequest);
            
            // Crear notificación de solicitud de seguimiento
            try {
                notificationService.createAndSendNotification(
                    target, 
                    me, 
                    com.team.socialnetwork.entity.Notification.NotificationType.FOLLOW_REQUEST, 
                    null, 
                    null
                );
            } catch (Exception e) {
                // Log el error pero no fallar la operación
                System.err.println("Error creating follow request notification: " + e.getMessage());
            }
            
            return ResponseEntity.status(org.springframework.http.HttpStatus.ACCEPTED)
                    .body(new com.team.socialnetwork.dto.MessageResponse("Follow request sent"));
        } else {
            me.getFollowing().add(target);
            userRepository.save(me);
            
            // Crear notificación de seguimiento
            try {
                notificationService.createAndSendNotification(
                    target, 
                    me, 
                    com.team.socialnetwork.entity.Notification.NotificationType.FOLLOW, 
                    null, 
                    null
                );
            } catch (Exception e) {
                // Log el error pero no fallar la operación
                System.err.println("Error creating follow notification: " + e.getMessage());
            }
            
            return ResponseEntity.ok(new com.team.socialnetwork.dto.MessageResponse("Followed successfully"));
        }
    }

    // Unfollow a user
    @DeleteMapping("/{userId}/follow")
    public ResponseEntity<com.team.socialnetwork.dto.MessageResponse> unfollow(Authentication authentication,
                                                                               @PathVariable Long userId) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.UNAUTHORIZED, "Missing or invalid token");
        }
        String email = authentication.getName();
        User me = userRepository.findByEmail(email)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND, "User not found"));

        User target = userRepository.findById(userId)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND, "User not found"));

        if (!me.getFollowing().contains(target)) {
            // If there is a pending request, allow cancel by deleting it
            java.util.Optional<FollowRequest> fr = followRequestRepository.findByFollowerIdAndTargetId(me.getId(), target.getId());
            if (fr.isPresent()) {
                followRequestRepository.delete(fr.get());
                
                // Eliminar notificación de solicitud de seguimiento
                notificationService.removeNotification(
                    target, 
                    me, 
                    com.team.socialnetwork.entity.Notification.NotificationType.FOLLOW_REQUEST
                );
                
                return ResponseEntity.ok(new com.team.socialnetwork.dto.MessageResponse("Follow request canceled"));
            }
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.CONFLICT, "Not following yet");
        }
        me.getFollowing().remove(target);
        userRepository.save(me);
        
        // Eliminar notificación de seguimiento
        notificationService.removeNotification(
            target, 
            me, 
            com.team.socialnetwork.entity.Notification.NotificationType.FOLLOW
        );
        
        return ResponseEntity.ok(new com.team.socialnetwork.dto.MessageResponse("Unfollowed successfully"));
    }

    // Approve a follow request from {userId} to me
    @PostMapping("/{userId}/follow/approve")
    public ResponseEntity<com.team.socialnetwork.dto.MessageResponse> approveFollow(Authentication authentication,
                                                                                    @PathVariable Long userId) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.UNAUTHORIZED, "Missing or invalid token");
        }
        String email = authentication.getName();
        User me = userRepository.findByEmail(email)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND, "User not found"));
        User follower = userRepository.findById(userId)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND, "Follower not found"));

        FollowRequest fr = followRequestRepository.findByFollowerIdAndTargetId(follower.getId(), me.getId())
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND, "Follow request not found"));
        followRequestRepository.delete(fr);
        // Create following relation
        follower.getFollowing().add(me);
        userRepository.save(follower);
        
        // Crear notificación de seguimiento aprobado
        notificationService.createAndSendNotification(
            follower, 
            me, 
            com.team.socialnetwork.entity.Notification.NotificationType.FOLLOW, 
            null, 
            null
        );
        
        return ResponseEntity.ok(new com.team.socialnetwork.dto.MessageResponse("Follow request approved"));
    }

    // Reject a follow request from {userId} to me
    @PostMapping("/{userId}/follow/reject")
    public ResponseEntity<com.team.socialnetwork.dto.MessageResponse> rejectFollow(Authentication authentication,
                                                                                   @PathVariable Long userId) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.UNAUTHORIZED, "Missing or invalid token");
        }
        String email = authentication.getName();
        User me = userRepository.findByEmail(email)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND, "User not found"));
        User follower = userRepository.findById(userId)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND, "Follower not found"));

        FollowRequest fr = followRequestRepository.findByFollowerIdAndTargetId(follower.getId(), me.getId())
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND, "Follow request not found"));
        followRequestRepository.delete(fr);
        return ResponseEntity.ok(new com.team.socialnetwork.dto.MessageResponse("Follow request rejected"));
    }

    // Remove a follower from my followers list
    @DeleteMapping("/me/followers/{followerId}")
    public ResponseEntity<com.team.socialnetwork.dto.MessageResponse> removeFollower(Authentication authentication,
                                                                                      @PathVariable Long followerId) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.UNAUTHORIZED, "Missing or invalid token");
        }
        String email = authentication.getName();
        User me = userRepository.findByEmail(email)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND, "User not found"));
        User follower = userRepository.findById(followerId)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND, "Follower not found"));

        // Verificar que realmente es mi seguidor
        if (!me.getFollowers().contains(follower) || !follower.getFollowing().contains(me)) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.CONFLICT, "User is not following you");
        }

        // Eliminar la relación de seguimiento (eliminar seguidor)
        me.getFollowers().remove(follower);
        follower.getFollowing().remove(me);
        
        userRepository.save(me);
        userRepository.save(follower);
        
        // Eliminar notificación de seguimiento que el seguidor pudo haber recibido
        notificationService.removeNotification(
            follower, 
            me, 
            com.team.socialnetwork.entity.Notification.NotificationType.FOLLOW
        );
        
        return ResponseEntity.ok(new com.team.socialnetwork.dto.MessageResponse("Follower removed successfully"));
    }

    // List all users (safe data)
    @GetMapping
    public ResponseEntity<java.util.List<SafeUser>> listUsers(@RequestParam(name = "q") String query) {
        if (query == null || query.trim().isEmpty()) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST, "Query parameter 'q' is required");
        }
        java.util.List<User> users = userRepository.searchByTerm(query.trim());
        java.util.List<SafeUser> resp = users.stream()
                .map(u -> new SafeUser(u.getId(), u.getFullName(), u.getUsername(), u.getEmail(), u.getCreatedAt()))
                .toList();
        return ResponseEntity.ok(resp);
    }

    // Get a user's public profile
    @GetMapping("/{userId:\\d+}")
    public ResponseEntity<PublicUserResponse> getUserById(@PathVariable Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND, "User not found"));
        long followersCount = user.getFollowers().size();
        long followingCount = user.getFollowing().size();
        PublicUserResponse dto = new PublicUserResponse(
                user.getId(), user.getFullName(), user.getUsername(), user.getEmail(), user.getCreatedAt(),
                followersCount, followingCount, user.isPrivate()
        );
        return ResponseEntity.ok(dto);
    }

    // List followers of a user
    @GetMapping("/{userId}/followers")
    public ResponseEntity<java.util.List<SafeUser>> listFollowers(@PathVariable Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND, "User not found"));
        java.util.List<SafeUser> resp = user.getFollowers().stream()
                .map(u -> new SafeUser(u.getId(), u.getFullName(), u.getUsername(), u.getEmail(), u.getCreatedAt()))
                .toList();
        return ResponseEntity.ok(resp);
    }

    // List following of a user
    @GetMapping("/{userId}/following")
    public ResponseEntity<java.util.List<SafeUser>> listFollowing(@PathVariable Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND, "User not found"));
        java.util.List<SafeUser> resp = user.getFollowing().stream()
                .map(u -> new SafeUser(u.getId(), u.getFullName(), u.getUsername(), u.getEmail(), u.getCreatedAt()))
                .toList();
        return ResponseEntity.ok(resp);
    }

    // Relationship between authenticated user and {userId}
    @GetMapping("/{userId}/relationship")
    public ResponseEntity<RelationshipResponse> relationship(Authentication authentication,
                                                            @PathVariable Long userId) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.UNAUTHORIZED, "Missing or invalid token");
        }
        String email = authentication.getName();
        User me = userRepository.findByEmail(email)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND, "User not found"));
        User target = userRepository.findById(userId)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND, "Target user not found"));

        boolean following = me.getFollowing().contains(target);
        boolean followsYou = target.getFollowing().contains(me);
        boolean requested = followRequestRepository.existsByFollowerIdAndTargetId(me.getId(), target.getId());
        boolean blocked = false; // not implemented yet
        return ResponseEntity.ok(new RelationshipResponse(following, followsYou, requested, blocked));
    }

    // Count followers of a user
    @GetMapping("/{userId}/followers/count")
    public ResponseEntity<java.util.Map<String, Long>> countFollowers(@PathVariable Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND, "User not found"));
        long count = user.getFollowers().size();
        java.util.Map<String, Long> body = new java.util.HashMap<>();
        body.put("count", count);
        return ResponseEntity.ok(body);
    }

    // Count following of a user
    @GetMapping("/{userId}/following/count")
    public ResponseEntity<java.util.Map<String, Long>> countFollowing(@PathVariable Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND, "User not found"));
        long count = user.getFollowing().size();
        java.util.Map<String, Long> body = new java.util.HashMap<>();
        body.put("count", count);
        return ResponseEntity.ok(body);
    }

    // List posts of a user (respect privacy)
    @GetMapping("/{userId}/posts")
    public ResponseEntity<java.util.List<PostResponse>> listUserPosts(Authentication authentication,
                                                                      @PathVariable Long userId) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.UNAUTHORIZED, "Missing or invalid token");
        }
        String email = authentication.getName();
        User me = userRepository.findByEmail(email)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND, "User not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND, "User not found"));
        if (user.isPrivate() && !user.getId().equals(me.getId()) && !me.getFollowing().contains(user)) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.FORBIDDEN, "This account is private");
        }
        java.util.List<Post> posts = postRepository.findByAuthorId(user.getId());
        java.util.List<PostResponse> resp = posts.stream()
                .map(p -> new PostResponse(p.getId(), p.getCreatedAt(), p.getDescription(), p.getImage(), p.getAuthor().getUsername(), p.getAuthor().getId()))
                .collect(java.util.stream.Collectors.toList());
        return ResponseEntity.ok(resp);
    }

    // Deprecated endpoint removed: comments listing moved to /posts/{postId}/comments

    @PatchMapping("/me/password")
    public ResponseEntity<com.team.socialnetwork.dto.MessageResponse> changePassword(
            Authentication authentication,
            @Valid @RequestBody ChangePasswordRequest request
    ) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.UNAUTHORIZED, "Missing or invalid token");
        }

        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND, "User not found"));

        // Verify current password
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.FORBIDDEN, "Incorrect password");
        }

        // Prevent reusing the same password
        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.CONFLICT, "New password must be different from the current one");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        return ResponseEntity.ok(new com.team.socialnetwork.dto.MessageResponse("Password updated successfully"));
    }

    @PatchMapping("/me/username")
    public ResponseEntity<com.team.socialnetwork.dto.MessageResponse> changeUsername(
            Authentication authentication,
            @Valid @RequestBody ChangeUsernameRequest request
    ) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.UNAUTHORIZED, "Missing or invalid token");
        }

        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND, "User not found"));

        String newUsername = request.getUsername();
        if (newUsername.equals(user.getUsername())) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST, "New username must be different from the current one");
        }

        if (userRepository.existsByUsername(newUsername)) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.CONFLICT, "Username already in use");
        }

        user.setUsername(newUsername);
        userRepository.save(user);
        return ResponseEntity.ok(new com.team.socialnetwork.dto.MessageResponse("Username updated successfully"));
    }

    // Optional: allow changing non-unique full name
    @PatchMapping("/me/full-name")
    public ResponseEntity<com.team.socialnetwork.dto.MessageResponse> changeFullName(
            Authentication authentication,
            @Valid @RequestBody ChangeNameRequest request
    ) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.UNAUTHORIZED, "Missing or invalid token");
        }

        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND, "User not found"));

        String newFullName = request.getName();
        if (newFullName.equals(user.getFullName())) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST, "New full name must be different from the current one");
        }

        user.setFullName(newFullName);
        userRepository.save(user);
        return ResponseEntity.ok(new com.team.socialnetwork.dto.MessageResponse("Full name updated successfully"));
    }

    @DeleteMapping("/me")
    public ResponseEntity<com.team.socialnetwork.dto.MessageResponse> deleteAccount(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.UNAUTHORIZED, "Missing or invalid token");
        }

        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND, "User not found"));

        // Ensure comment likes (not mapped in entity) are removed first
        commentLikeRepository.deleteByUserId(user.getId());

        userRepository.delete(user);
        return ResponseEntity.ok(new com.team.socialnetwork.dto.MessageResponse("Account deleted successfully"));
    }

    @PatchMapping("/me/profile")
    public ResponseEntity<com.team.socialnetwork.dto.MessageResponse> updateProfile(
            Authentication authentication,
            @RequestBody UpdateProfileRequest request
    ) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.UNAUTHORIZED, "Missing or invalid token");
        }

        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND, "User not found"));

        // For now only supports fullName; future: bio, avatar, location
        if (request.getFullName() != null) {
            String newName = request.getFullName().trim();
            user.setFullName(newName.isEmpty() ? null : newName);
        }

        userRepository.save(user);
        return ResponseEntity.ok(new com.team.socialnetwork.dto.MessageResponse("Profile updated successfully"));
    }

    /**
     * Obtener solicitudes de seguimiento pendientes para el usuario autenticado
     * Solo aplicable si el usuario tiene perfil privado
     */
    @GetMapping("/me/follow-requests")
    public ResponseEntity<java.util.List<FollowRequestResponse>> getPendingFollowRequests(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.UNAUTHORIZED, "Missing or invalid token");
        }

        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND, "User not found"));

        // Verificar que el usuario tenga perfil privado
        if (!user.isPrivate()) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST, "User profile is not private");
        }

        // Obtener todas las solicitudes pendientes
        java.util.List<com.team.socialnetwork.entity.FollowRequest> requests = 
                followRequestRepository.findByTargetId(user.getId());

        // Convertir a DTO
        java.util.List<FollowRequestResponse> response = requests.stream()
                .map(request -> new FollowRequestResponse(
                        request.getId(),
                        request.getFollower().getId(),
                        request.getFollower().getUsername(),
                        request.getFollower().getFullName(),
                        request.getCreatedAt()
                ))
                .collect(java.util.stream.Collectors.toList());

        return ResponseEntity.ok(response);
    }

    /**
     * Manejar solicitud de seguimiento (aceptar o rechazar)
     */
    @PatchMapping("/me/follow-requests/{requestId}")
    public ResponseEntity<com.team.socialnetwork.dto.MessageResponse> handleFollowRequest(
            Authentication authentication,
            @PathVariable Long requestId,
            @jakarta.validation.Valid @RequestBody FollowRequestActionRequest actionRequest) {
        
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.UNAUTHORIZED, "Missing or invalid token");
        }

        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND, "User not found"));

        // Buscar la solicitud
        com.team.socialnetwork.entity.FollowRequest request = followRequestRepository.findById(requestId)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND, "Follow request not found"));

        // Verificar que la solicitud sea para el usuario autenticado
        if (!request.getTarget().getId().equals(user.getId())) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.FORBIDDEN, "This follow request is not for you");
        }

        String action = actionRequest.getAction();
        if (action == null || (!action.equals("accept") && !action.equals("reject"))) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST, "Action must be 'accept' or 'reject'");
        }

        if (action.equals("accept")) {
            // Aceptar: añadir a la lista de seguidores
            User follower = request.getFollower();
            User target = request.getTarget();
            
            // Añadir la relación de seguimiento
            follower.getFollowing().add(target);
            target.getFollowers().add(follower);
            
            userRepository.save(follower);
            userRepository.save(target);
            
            // Eliminar la solicitud
            followRequestRepository.delete(request);
            
            // Crear notificación de seguimiento aprobado
            notificationService.createAndSendNotification(
                follower, 
                target, 
                com.team.socialnetwork.entity.Notification.NotificationType.FOLLOW, 
                null, 
                null
            );
            
            return ResponseEntity.ok(new com.team.socialnetwork.dto.MessageResponse("Follow request accepted"));
        } else {
            // Rechazar: solo eliminar la solicitud
            followRequestRepository.delete(request);
            return ResponseEntity.ok(new com.team.socialnetwork.dto.MessageResponse("Follow request rejected"));
        }
    }
}
