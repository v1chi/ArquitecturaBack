package com.team.socialnetwork.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
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

import com.team.socialnetwork.dto.MessageResponse;
import com.team.socialnetwork.dto.SafeUser;
import com.team.socialnetwork.entity.Comment;
import com.team.socialnetwork.entity.CommentLike;
import com.team.socialnetwork.entity.Post;
import com.team.socialnetwork.entity.User;
import com.team.socialnetwork.repository.CommentLikeRepository;
import com.team.socialnetwork.repository.CommentRepository;
import com.team.socialnetwork.repository.UserRepository;
import com.team.socialnetwork.service.NotificationService;

@ExtendWith(MockitoExtension.class)
class CommentsControllerTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CommentLikeRepository commentLikeRepository;

    @Mock
    private NotificationService notificationService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private CommentsController commentsController;

    private User testUser;
    private User postAuthor;
    private Post testPost;
    private Comment testComment;

    @BeforeEach
    void setUp() {
        testUser = new User("testuser", "test@example.com", "password123");
        testUser.setId(1L);
        testUser.setFullName("Test User");

        postAuthor = new User("author", "author@example.com", "pass456");
        postAuthor.setId(2L);
        postAuthor.setFullName("Post Author");

        testPost = new Post("Test post", "image.jpg", postAuthor);
        testPost.setId(1L);

        testComment = new Comment("Nice post!", testPost, testUser);
        testComment.setId(1L);
    }

    @Test
    void testDeleteComment_Success() {
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(commentRepository.findById(1L)).thenReturn(Optional.of(testComment));

        ResponseEntity<MessageResponse> response = commentsController.deleteComment(authentication, 1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Comment deleted successfully", response.getBody().getMessage());
        verify(commentLikeRepository).deleteByCommentId(1L);
        verify(commentRepository).delete(testComment);
    }

    @Test
    void testDeleteComment_NotOwner() {
        User anotherUser = new User("another", "another@example.com", "pass");
        anotherUser.setId(3L);
        
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("another@example.com");
        when(userRepository.findByEmail("another@example.com")).thenReturn(Optional.of(anotherUser));
        when(commentRepository.findById(1L)).thenReturn(Optional.of(testComment));

        assertThrows(ResponseStatusException.class, () -> 
            commentsController.deleteComment(authentication, 1L)
        );
    }

    @Test
    void testDeleteComment_PrivateAccount() {
        postAuthor.setPrivate(true);
        
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(commentRepository.findById(1L)).thenReturn(Optional.of(testComment));

        assertThrows(ResponseStatusException.class, () -> 
            commentsController.deleteComment(authentication, 1L)
        );
    }

    @Test
    void testCountCommentLikes_Success() {
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(commentRepository.findById(1L)).thenReturn(Optional.of(testComment));
        when(commentLikeRepository.countByCommentId(1L)).thenReturn(5L);

        ResponseEntity<Map<String, Long>> response = commentsController.countCommentLikes(authentication, 1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(5L, response.getBody().get("count"));
    }

    @Test
    void testListCommentLikes_Success() {
        User liker = new User("liker", "liker@example.com", "pass");
        liker.setId(3L);
        liker.setFullName("Liker");
        
        CommentLike commentLike = new CommentLike(liker, testComment);
        Page<CommentLike> likesPage = new PageImpl<>(Arrays.asList(commentLike));

        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(commentRepository.findById(1L)).thenReturn(Optional.of(testComment));
        when(commentLikeRepository.findByCommentId(eq(1L), any(Pageable.class))).thenReturn(likesPage);

        ResponseEntity<List<SafeUser>> response = commentsController.listCommentLikes(authentication, 1L, 0, 10);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void testLikeComment_Success() {
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(commentRepository.findById(1L)).thenReturn(Optional.of(testComment));
        when(commentLikeRepository.existsByUserIdAndCommentId(1L, 1L)).thenReturn(false);

        ResponseEntity<MessageResponse> response = commentsController.likeComment(authentication, 1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Comment liked successfully", response.getBody().getMessage());
        verify(commentLikeRepository).save(any(CommentLike.class));
    }

    @Test
    void testLikeComment_AlreadyLiked() {
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(commentRepository.findById(1L)).thenReturn(Optional.of(testComment));
        when(commentLikeRepository.existsByUserIdAndCommentId(1L, 1L)).thenReturn(true);

        assertThrows(ResponseStatusException.class, () -> 
            commentsController.likeComment(authentication, 1L)
        );
    }

    @Test
    void testLikeComment_PrivateAccount() {
        postAuthor.setPrivate(true);
        
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(commentRepository.findById(1L)).thenReturn(Optional.of(testComment));

        assertThrows(ResponseStatusException.class, () -> 
            commentsController.likeComment(authentication, 1L)
        );
    }

    @Test
    void testUnlikeComment_Success() {
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(commentRepository.findById(1L)).thenReturn(Optional.of(testComment));
        when(commentLikeRepository.deleteByUserIdAndCommentId(1L, 1L)).thenReturn(1);

        ResponseEntity<MessageResponse> response = commentsController.unlikeComment(authentication, 1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Comment unliked successfully", response.getBody().getMessage());
    }

    @Test
    void testUnlikeComment_NotLiked() {
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(commentRepository.findById(1L)).thenReturn(Optional.of(testComment));
        when(commentLikeRepository.deleteByUserIdAndCommentId(1L, 1L)).thenReturn(0);

        assertThrows(ResponseStatusException.class, () -> 
            commentsController.unlikeComment(authentication, 1L)
        );
    }

    @Test
    void testCheckCommentLike_Success() {
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(commentRepository.findById(1L)).thenReturn(Optional.of(testComment));
        when(commentLikeRepository.existsByUserIdAndCommentId(1L, 1L)).thenReturn(true);

        ResponseEntity<Map<String, Boolean>> response = commentsController.checkCommentLike(authentication, 1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().get("liked"));
    }

    @Test
    void testCheckCommentLike_NotLiked() {
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(commentRepository.findById(1L)).thenReturn(Optional.of(testComment));
        when(commentLikeRepository.existsByUserIdAndCommentId(1L, 1L)).thenReturn(false);

        ResponseEntity<Map<String, Boolean>> response = commentsController.checkCommentLike(authentication, 1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertFalse(response.getBody().get("liked"));
    }

    @Test
    void testCommentNotFound() {
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(commentRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> 
            commentsController.deleteComment(authentication, 999L)
        );
    }

    @Test
    void testUnauthorized() {
        when(authentication.isAuthenticated()).thenReturn(false);

        assertThrows(ResponseStatusException.class, () -> 
            commentsController.deleteComment(authentication, 1L)
        );
    }
}
