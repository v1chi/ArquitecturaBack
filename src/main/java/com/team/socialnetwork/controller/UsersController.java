package com.team.socialnetwork.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.DeleteMapping;

import com.team.socialnetwork.dto.ChangePasswordRequest;
import com.team.socialnetwork.dto.CommentResponse;
import com.team.socialnetwork.dto.PostResponse;
import com.team.socialnetwork.dto.SafeUser;
import com.team.socialnetwork.dto.ChangeUsernameRequest;
import com.team.socialnetwork.dto.ChangeNameRequest;
import com.team.socialnetwork.entity.Post;
import com.team.socialnetwork.entity.User;
import com.team.socialnetwork.repository.CommentRepository;
import com.team.socialnetwork.repository.PostRepository;
import com.team.socialnetwork.repository.UserRepository;
import com.team.socialnetwork.repository.CommentLikeRepository;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/users")
public class UsersController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final CommentLikeRepository commentLikeRepository;

    public UsersController(UserRepository userRepository, PasswordEncoder passwordEncoder,
                           PostRepository postRepository, CommentRepository commentRepository,
                           CommentLikeRepository commentLikeRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
        this.commentLikeRepository = commentLikeRepository;
    }

    @GetMapping("/me")
    public ResponseEntity<SafeUser> me(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.UNAUTHORIZED, "Missing or invalid token");
        }
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND, "User not found"));
        SafeUser dto = new SafeUser(user.getId(), user.getFullName(), user.getUsername(), user.getEmail(), user.getCreatedAt());
        return ResponseEntity.ok(dto);
    }

    // List all users (safe data)
    @GetMapping
    public ResponseEntity<java.util.List<SafeUser>> listUsers() {
        java.util.List<User> users = userRepository.findAll();
        java.util.List<SafeUser> resp = users.stream()
                .map(u -> new SafeUser(u.getId(), u.getFullName(), u.getUsername(), u.getEmail(), u.getCreatedAt()))
                .toList();
        return ResponseEntity.ok(resp);
    }

    // List posts of a user
    @GetMapping("/{userId}/posts")
    public ResponseEntity<java.util.List<PostResponse>> listUserPosts(@PathVariable Long userId) {
        // ensure user exists (optional but clearer errors)
        userRepository.findById(userId)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND, "User not found"));
        java.util.List<Post> posts = postRepository.findByAuthorId(userId);
        java.util.List<PostResponse> resp = posts.stream()
                .map(p -> new PostResponse(p.getId(), p.getCreatedAt(), p.getDescription(), p.getImage()))
                .toList();
        return ResponseEntity.ok(resp);
    }

    // List comments for a user's post (verify ownership)
    @GetMapping("/{userId}/posts/{postId}/comments")
    public ResponseEntity<java.util.List<CommentResponse>> listCommentsForUserPost(@PathVariable Long userId,
                                                                                   @PathVariable Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND, "Post not found"));
        if (!post.getAuthor().getId().equals(userId)) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST, "Post does not belong to the specified user");
        }
        java.util.List<com.team.socialnetwork.entity.Comment> comments = commentRepository.findByPostId(postId);
        java.util.List<CommentResponse> resp = comments.stream()
                .map(c -> new CommentResponse(c.getId(), c.getCreatedAt(), c.getText()))
                .toList();
        return ResponseEntity.ok(resp);
    }

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
}
