package com.team.socialnetwork.controller;

import com.team.socialnetwork.dto.CreatePostRequest;
import com.team.socialnetwork.dto.CreateCommentRequest;
import com.team.socialnetwork.dto.PostResponse;
import com.team.socialnetwork.entity.Post;
import com.team.socialnetwork.entity.User;
import com.team.socialnetwork.entity.Comment;
import com.team.socialnetwork.repository.PostRepository;
import com.team.socialnetwork.repository.UserRepository;
import com.team.socialnetwork.repository.CommentRepository;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/posts")
public class PostsController {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;

    public PostsController(PostRepository postRepository, UserRepository userRepository,
                           CommentRepository commentRepository) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.commentRepository = commentRepository;
    }

    @PostMapping
    public ResponseEntity<com.team.socialnetwork.dto.MessageResponse> create(Authentication authentication,
                                               @Valid @RequestBody CreatePostRequest request) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.UNAUTHORIZED, "Missing or invalid token");
        }
        String email = authentication.getName();
        User author = userRepository.findByEmail(email)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND, "User not found"));

        Post post = new Post(request.getDescription(), request.getImage(), author);
        postRepository.save(post);
        return ResponseEntity.ok(new com.team.socialnetwork.dto.MessageResponse("Post created successfully"));
    }

    @org.springframework.web.bind.annotation.PostMapping("/{postId}/comments")
    public ResponseEntity<com.team.socialnetwork.dto.MessageResponse> createComment(Authentication authentication,
                                                                                   @org.springframework.web.bind.annotation.PathVariable Long postId,
                                                                                   @Valid @RequestBody CreateCommentRequest request) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.UNAUTHORIZED, "Missing or invalid token");
        }
        String email = authentication.getName();
        User author = userRepository.findByEmail(email)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND, "User not found"));

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND, "Post not found"));

        Comment comment = new Comment(request.getText(), post, author);
        commentRepository.save(comment);
        return ResponseEntity.ok(new com.team.socialnetwork.dto.MessageResponse("Comment created successfully"));
    }

    @org.springframework.web.bind.annotation.DeleteMapping("/{id}")
    public ResponseEntity<com.team.socialnetwork.dto.MessageResponse> delete(Authentication authentication,
                                                                            @org.springframework.web.bind.annotation.PathVariable Long id) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.UNAUTHORIZED, "Missing or invalid token");
        }
        String email = authentication.getName();
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND, "Post not found"));
        if (!post.getAuthor().getEmail().equals(email)) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.FORBIDDEN, "You can only delete your own posts");
        }
        postRepository.delete(post);
        return ResponseEntity.ok(new com.team.socialnetwork.dto.MessageResponse("Post deleted successfully"));
    }

    @org.springframework.web.bind.annotation.GetMapping("/mine")
    public ResponseEntity<java.util.List<PostResponse>> myPosts(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.UNAUTHORIZED, "Missing or invalid token");
        }
        String email = authentication.getName();
        User me = userRepository.findByEmail(email)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND, "User not found"));
        java.util.List<Post> posts = postRepository.findByAuthorId(me.getId());
        java.util.List<PostResponse> resp = posts.stream()
                .map(p -> new PostResponse(p.getId(), p.getCreatedAt(), p.getDescription(), p.getImage()))
                .toList();
        return ResponseEntity.ok(resp);
    }
}
