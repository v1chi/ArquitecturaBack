package com.team.socialnetwork.controller;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.server.ResponseStatusException;

import com.team.socialnetwork.dto.CommentResponse;
import com.team.socialnetwork.dto.CreateCommentRequest;
import com.team.socialnetwork.dto.CreatePostRequest;
import com.team.socialnetwork.dto.MessageResponse;
import com.team.socialnetwork.dto.PostDetailResponse;
import com.team.socialnetwork.dto.PostResponse;
import com.team.socialnetwork.dto.SafeUser;
import com.team.socialnetwork.entity.Comment;
import com.team.socialnetwork.entity.Post;
import com.team.socialnetwork.entity.PostLike;
import com.team.socialnetwork.entity.User;
import com.team.socialnetwork.repository.CommentRepository;
import com.team.socialnetwork.repository.PostLikeRepository;
import com.team.socialnetwork.repository.PostRepository;
import com.team.socialnetwork.repository.UserRepository;
import com.team.socialnetwork.service.NotificationService;

@ExtendWith(MockitoExtension.class)
class PostsControllerTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private PostLikeRepository postLikeRepository;

    @Mock
    private NotificationService notificationService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private PostsController postsController;

    private User testUser;
    private Post testPost;

    @BeforeEach
    void setUp() {
        testUser = new User("testuser", "test@example.com", "password123");
        testUser.setId(1L);
        testUser.setFullName("Test User");
        
        testPost = new Post("Test description", "image.jpg", testUser);
        testPost.setId(1L);
    }

    @Test
    void testCreatePost_Success() {
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(postRepository.save(any(Post.class))).thenReturn(testPost);

        CreatePostRequest request = new CreatePostRequest();
        request.setDescription("Test post");
        request.setImage("image.jpg");
        ResponseEntity<MessageResponse> response = postsController.create(authentication, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Post created successfully", response.getBody().getMessage());
        verify(postRepository).save(any(Post.class));
    }

    @Test
    void testCreatePost_Unauthorized() {
        when(authentication.isAuthenticated()).thenReturn(false);

        CreatePostRequest request = new CreatePostRequest();
        request.setDescription("Test post");
        request.setImage("image.jpg");
        
        assertThrows(ResponseStatusException.class, () -> 
            postsController.create(authentication, request)
        );
    }

    @Test
    void testDeletePost_Success() {
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("test@example.com");
        when(postRepository.findById(1L)).thenReturn(Optional.of(testPost));

        ResponseEntity<MessageResponse> response = postsController.delete(authentication, 1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Post deleted successfully", response.getBody().getMessage());
        verify(postRepository).delete(testPost);
    }

    @Test
    void testDeletePost_NotOwner() {
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("other@example.com");
        when(postRepository.findById(1L)).thenReturn(Optional.of(testPost));

        assertThrows(ResponseStatusException.class, () -> 
            postsController.delete(authentication, 1L)
        );
    }

    @Test
    void testGetPost_Success() {
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(postRepository.findById(1L)).thenReturn(Optional.of(testPost));
        when(postLikeRepository.countByPostId(1L)).thenReturn(5L);
        when(commentRepository.findByPostId(1L)).thenReturn(Collections.emptyList());
        when(postLikeRepository.existsByUserIdAndPostId(1L, 1L)).thenReturn(false);

        ResponseEntity<PostDetailResponse> response = postsController.getPost(authentication, 1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(5L, response.getBody().getLikesCount());
    }

    @Test
    void testMyPosts_Success() {
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(postRepository.findByAuthorId(1L)).thenReturn(Arrays.asList(testPost));

        ResponseEntity<List<PostResponse>> response = postsController.myPosts(authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void testLikePost_Success() {
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(postRepository.findById(1L)).thenReturn(Optional.of(testPost));
        when(postLikeRepository.existsByUserIdAndPostId(1L, 1L)).thenReturn(false);

        ResponseEntity<MessageResponse> response = postsController.likePost(authentication, 1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Post liked successfully", response.getBody().getMessage());
        verify(postLikeRepository).save(any(PostLike.class));
    }

    @Test
    void testLikePost_AlreadyLiked() {
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(postRepository.findById(1L)).thenReturn(Optional.of(testPost));
        when(postLikeRepository.existsByUserIdAndPostId(1L, 1L)).thenReturn(true);

        assertThrows(ResponseStatusException.class, () -> 
            postsController.likePost(authentication, 1L)
        );
    }

    @Test
    void testUnlikePost_Success() {
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(postRepository.findById(1L)).thenReturn(Optional.of(testPost));
        when(postLikeRepository.deleteByUserIdAndPostId(1L, 1L)).thenReturn(1);

        ResponseEntity<MessageResponse> response = postsController.unlikePost(authentication, 1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Post unliked successfully", response.getBody().getMessage());
    }

    @Test
    void testUnlikePost_NotLiked() {
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(postRepository.findById(1L)).thenReturn(Optional.of(testPost));
        when(postLikeRepository.deleteByUserIdAndPostId(1L, 1L)).thenReturn(0);

        assertThrows(ResponseStatusException.class, () -> 
            postsController.unlikePost(authentication, 1L)
        );
    }

    @Test
    void testCountPostLikes_Success() {
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(postRepository.findById(1L)).thenReturn(Optional.of(testPost));
        when(postLikeRepository.countByPostId(1L)).thenReturn(10L);

        ResponseEntity<Map<String, Long>> response = postsController.countPostLikes(authentication, 1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(10L, response.getBody().get("count"));
    }

    @Test
    void testCheckPostLike_Success() {
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(postRepository.findById(1L)).thenReturn(Optional.of(testPost));
        when(postLikeRepository.existsByUserIdAndPostId(1L, 1L)).thenReturn(true);

        ResponseEntity<Map<String, Boolean>> response = postsController.checkPostLike(authentication, 1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().get("liked"));
    }

    @Test
    void testListPostLikes_Success() {
        User liker = new User("liker", "liker@example.com", "pass");
        liker.setId(2L);
        liker.setFullName("Liker");
        PostLike postLike = new PostLike(liker, testPost);
        Page<PostLike> likesPage = new PageImpl<>(Arrays.asList(postLike));

        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(postRepository.findById(1L)).thenReturn(Optional.of(testPost));
        when(postLikeRepository.findByPostId(eq(1L), any(Pageable.class))).thenReturn(likesPage);

        ResponseEntity<List<SafeUser>> response = postsController.listPostLikes(authentication, 1L, 0, 10);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void testCreateComment_Success() {
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(postRepository.findById(1L)).thenReturn(Optional.of(testPost));

        CreateCommentRequest request = new CreateCommentRequest();
        request.setText("Nice post!");
        ResponseEntity<MessageResponse> response = postsController.createComment(authentication, 1L, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Comment created successfully", response.getBody().getMessage());
        verify(commentRepository).save(any(Comment.class));
    }

    @Test
    void testListCommentsForPost_Success() {
        Comment comment = new Comment("Great!", testPost, testUser);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(postRepository.findById(1L)).thenReturn(Optional.of(testPost));
        when(commentRepository.findByPostId(1L)).thenReturn(Arrays.asList(comment));

        ResponseEntity<List<CommentResponse>> response = postsController.listCommentsForPost(authentication, 1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void testFeed_EmptyFollowing() {
        testUser.setFollowing(new HashSet<>());
        
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        ResponseEntity<List<PostDetailResponse>> response = postsController.feed(authentication, 0, 10);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isEmpty());
    }

    @Test
    void testFeed_WithFollowing() {
        User followedUser = new User("followed", "followed@example.com", "pass");
        followedUser.setId(2L);
        followedUser.setFullName("Followed");
        testUser.setFollowing(new HashSet<>(Arrays.asList(followedUser)));
        
        Post followedPost = new Post("Followed post", "img.jpg", followedUser);
        followedPost.setId(2L);
        
        Page<Post> postsPage = new PageImpl<>(Arrays.asList(followedPost));
        
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(postRepository.findByAuthorIdIn(anyList(), any(Pageable.class))).thenReturn(postsPage);
        when(postLikeRepository.countByPostIds(anyList())).thenReturn(Collections.emptyList());
        when(commentRepository.countByPostIds(anyList())).thenReturn(Collections.emptyList());
        when(postLikeRepository.findPostIdsLikedByUser(anyLong(), anyList())).thenReturn(Collections.emptyList());

        ResponseEntity<List<PostDetailResponse>> response = postsController.feed(authentication, 0, 10);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void testCreateComment_PrivateAccountNotFollowing() {
        User privateUser = new User("private", "private@example.com", "pass");
        privateUser.setId(2L);
        privateUser.setFullName("Private");
        privateUser.setPrivate(true);
        
        Post privatePost = new Post("Private post", null, privateUser);
        privatePost.setId(2L);
        
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(postRepository.findById(2L)).thenReturn(Optional.of(privatePost));

        CreateCommentRequest request = new CreateCommentRequest();
        request.setText("Nice!");
        
        assertThrows(ResponseStatusException.class, () -> 
            postsController.createComment(authentication, 2L, request)
        );
    }

    @Test
    void testFeed_InvalidPage() {
        when(authentication.isAuthenticated()).thenReturn(true);

        assertThrows(ResponseStatusException.class, () -> 
            postsController.feed(authentication, -1, 10)
        );
    }

    @Test
    void testFeed_InvalidSize() {
        when(authentication.isAuthenticated()).thenReturn(true);

        assertThrows(ResponseStatusException.class, () -> 
            postsController.feed(authentication, 0, 0)
        );
    }
}
