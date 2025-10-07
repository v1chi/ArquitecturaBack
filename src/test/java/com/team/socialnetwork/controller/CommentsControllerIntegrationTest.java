package com.team.socialnetwork.controller;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.hasSize;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.transaction.annotation.Transactional;

import com.team.socialnetwork.entity.Comment;
import com.team.socialnetwork.entity.Post;
import com.team.socialnetwork.entity.User;
import com.team.socialnetwork.repository.CommentLikeRepository;
import com.team.socialnetwork.repository.CommentRepository;
import com.team.socialnetwork.repository.PostRepository;
import com.team.socialnetwork.repository.UserRepository;
import com.team.socialnetwork.security.JwtService;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class CommentsControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private CommentLikeRepository commentLikeRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    private String token;
    private User testUser;
    private Post testPost;
    private Comment testComment;

    @BeforeEach
    void setUp() {
        // Limpiar datos
        commentLikeRepository.deleteAll();
        commentRepository.deleteAll();
        postRepository.deleteAll();
        userRepository.deleteAll();

        // Crear usuario de prueba
        testUser = new User("testuser", "test@example.com", passwordEncoder.encode("password123"));
        testUser.setFullName("Test User");
        testUser.setEmailConfirmed(true);
        testUser = userRepository.save(testUser);

        // Crear post de prueba
        testPost = new Post("Test post", null, testUser);
        testPost = postRepository.save(testPost);

        // Crear comentario de prueba
        testComment = new Comment("Test comment", testPost, testUser);
        testComment = commentRepository.save(testComment);

        // Generar token JWT
        Map<String, Object> claims = new HashMap<>();
        token = jwtService.generateAccessToken(testUser.getEmail(), claims);
    }

    @Test
    void testLikeComment() throws Exception {
        mockMvc.perform(post("/comments/" + testComment.getId() + "/likes")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Comment liked successfully"));
    }

    @Test
    void testLikeCommentAlreadyLiked() throws Exception {
        // Like primero
        mockMvc.perform(post("/comments/" + testComment.getId() + "/likes")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        // Intentar like de nuevo
        mockMvc.perform(post("/comments/" + testComment.getId() + "/likes")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isConflict());
    }

    @Test
    void testLikeCommentNotFound() throws Exception {
        mockMvc.perform(post("/comments/99999/likes")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void testUnlikeComment() throws Exception {
        // Like primero
        mockMvc.perform(post("/comments/" + testComment.getId() + "/likes")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        // Unlike
        mockMvc.perform(delete("/comments/" + testComment.getId() + "/likes")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Comment unliked successfully"));
    }

    @Test
    void testUnlikeCommentNotLiked() throws Exception {
        mockMvc.perform(delete("/comments/" + testComment.getId() + "/likes")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isConflict());
    }

    @Test
    void testCountCommentLikes() throws Exception {
        // Like el comentario
        mockMvc.perform(post("/comments/" + testComment.getId() + "/likes")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        // Contar likes
        mockMvc.perform(get("/comments/" + testComment.getId() + "/likes/count")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(1));
    }

    @Test
    void testListCommentLikes() throws Exception {
        // Like el comentario
        mockMvc.perform(post("/comments/" + testComment.getId() + "/likes")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        // Listar usuarios que dieron like
        mockMvc.perform(get("/comments/" + testComment.getId() + "/likes")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].username").value("testuser"));
    }

    @Test
    void testCheckCommentLike() throws Exception {
        // Sin like
        mockMvc.perform(get("/comments/" + testComment.getId() + "/likes/check")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.liked").value(false));

        // Dar like
        mockMvc.perform(post("/comments/" + testComment.getId() + "/likes")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        // Con like
        mockMvc.perform(get("/comments/" + testComment.getId() + "/likes/check")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.liked").value(true));
    }

    @Test
    void testDeleteComment() throws Exception {
        mockMvc.perform(delete("/comments/" + testComment.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Comment deleted successfully"));
    }

    @Test
    void testDeleteCommentNotOwner() throws Exception {
        // Crear otro usuario
        User otherUser = new User("otheruser", "other@example.com", passwordEncoder.encode("password123"));
        otherUser.setEmailConfirmed(true);
        otherUser = userRepository.save(otherUser);
        Map<String, Object> claims = new HashMap<>();
        String otherToken = jwtService.generateAccessToken(otherUser.getEmail(), claims);

        mockMvc.perform(delete("/comments/" + testComment.getId())
                        .header("Authorization", "Bearer " + otherToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void testDeleteCommentNotFound() throws Exception {
        mockMvc.perform(delete("/comments/99999")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void testLikeCommentOnPrivatePost() throws Exception {
        // Crear usuario privado
        User privateUser = new User("private", "private@example.com", passwordEncoder.encode("password123"));
        privateUser.setPrivate(true);
        privateUser.setEmailConfirmed(true);
        privateUser = userRepository.save(privateUser);

        // Crear post privado
        Post privatePost = new Post("Private post", null, privateUser);
        privatePost = postRepository.save(privatePost);

        // Crear comentario en post privado
        Comment privateComment = new Comment("Comment on private", privatePost, privateUser);
        privateComment = commentRepository.save(privateComment);

        // Intentar dar like sin seguir
        mockMvc.perform(post("/comments/" + privateComment.getId() + "/likes")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }
}
