package com.team.socialnetwork.controller;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.hasSize;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.team.socialnetwork.dto.CreateCommentRequest;
import com.team.socialnetwork.dto.CreatePostRequest;
import com.team.socialnetwork.entity.Post;
import com.team.socialnetwork.entity.User;
import com.team.socialnetwork.repository.CommentRepository;
import com.team.socialnetwork.repository.PostLikeRepository;
import com.team.socialnetwork.repository.PostRepository;
import com.team.socialnetwork.repository.UserRepository;
import com.team.socialnetwork.security.JwtService;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class PostsControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private PostLikeRepository postLikeRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    private String token;
    private User testUser;
    private Post testPost;

    @BeforeEach
    void setUp() {
        // Limpiar datos
        commentRepository.deleteAll();
        postLikeRepository.deleteAll();
        postRepository.deleteAll();
        userRepository.deleteAll();

        // Crear usuario de prueba
        testUser = new User("testuser", "test@example.com", passwordEncoder.encode("password123"));
        testUser.setFullName("Test User");
        testUser.setEmailConfirmed(true);
        testUser = userRepository.save(testUser);

        // Generar token JWT
        Map<String, Object> claims = new HashMap<>();
        token = jwtService.generateAccessToken(testUser.getEmail(), claims);

        // Crear un post de prueba
        testPost = new Post("Test post content", "test-image.jpg", testUser);
        testPost = postRepository.save(testPost);
    }

    @Test
    void testCreatePost() throws Exception {
        CreatePostRequest request = new CreatePostRequest();
        request.setDescription("New post content");
        request.setImage("new-image.jpg");

        mockMvc.perform(post("/posts")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Post created successfully"));
    }

    @Test
    void testCreatePostUnauthorized() throws Exception {
        CreatePostRequest request = new CreatePostRequest();
        request.setDescription("New post");

        mockMvc.perform(post("/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testGetPost() throws Exception {
        mockMvc.perform(get("/posts/" + testPost.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testPost.getId()))
                .andExpect(jsonPath("$.description").value("Test post content"))
                .andExpect(jsonPath("$.author.username").value("testuser"));
    }

    @Test
    void testGetPostNotFound() throws Exception {
        mockMvc.perform(get("/posts/99999")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void testDeletePost() throws Exception {
        mockMvc.perform(delete("/posts/" + testPost.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Post deleted successfully"));
    }

    @Test
    void testDeletePostNotOwner() throws Exception {
        User otherUser = new User("otheruser", "other@example.com", passwordEncoder.encode("password123"));
        otherUser.setEmailConfirmed(true);
        otherUser = userRepository.save(otherUser);
        Map<String, Object> claims = new HashMap<>();
        String otherToken = jwtService.generateAccessToken(otherUser.getEmail(), claims);

        mockMvc.perform(delete("/posts/" + testPost.getId())
                        .header("Authorization", "Bearer " + otherToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void testMyPosts() throws Exception {
        mockMvc.perform(get("/posts/mine")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].description").value("Test post content"));
    }

    @Test
    void testLikePost() throws Exception {
        mockMvc.perform(post("/posts/" + testPost.getId() + "/likes")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Post liked successfully"));
    }

    @Test
    void testLikePostAlreadyLiked() throws Exception {
        // Like primero
        mockMvc.perform(post("/posts/" + testPost.getId() + "/likes")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        // Intentar like de nuevo
        mockMvc.perform(post("/posts/" + testPost.getId() + "/likes")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isConflict());
    }

    @Test
    void testUnlikePost() throws Exception {
        // Like primero
        mockMvc.perform(post("/posts/" + testPost.getId() + "/likes")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        // Unlike
        mockMvc.perform(delete("/posts/" + testPost.getId() + "/likes")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Post unliked successfully"));
    }

    @Test
    void testCountPostLikes() throws Exception {
        // Like el post
        mockMvc.perform(post("/posts/" + testPost.getId() + "/likes")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        // Contar likes
        mockMvc.perform(get("/posts/" + testPost.getId() + "/likes/count")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(1));
    }

    @Test
    void testListPostLikes() throws Exception {
        // Like el post
        mockMvc.perform(post("/posts/" + testPost.getId() + "/likes")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        // Listar usuarios que dieron like
        mockMvc.perform(get("/posts/" + testPost.getId() + "/likes")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].username").value("testuser"));
    }

    @Test
    void testCheckPostLike() throws Exception {
        // Sin like
        mockMvc.perform(get("/posts/" + testPost.getId() + "/likes/check")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.liked").value(false));

        // Dar like
        mockMvc.perform(post("/posts/" + testPost.getId() + "/likes")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        // Con like
        mockMvc.perform(get("/posts/" + testPost.getId() + "/likes/check")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.liked").value(true));
    }

    @Test
    void testCreateComment() throws Exception {
        CreateCommentRequest request = new CreateCommentRequest();
        request.setText("Great post!");

        mockMvc.perform(post("/posts/" + testPost.getId() + "/comments")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Comment created successfully"));
    }

    @Test
    void testListCommentsForPost() throws Exception {
        // Crear un comentario primero
        CreateCommentRequest request = new CreateCommentRequest();
        request.setText("Nice!");

        mockMvc.perform(post("/posts/" + testPost.getId() + "/comments")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        // Listar comentarios
        mockMvc.perform(get("/posts/" + testPost.getId() + "/comments")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].text").value("Nice!"));
    }

    @Test
    void testFeedWithFollowing() throws Exception {
        // Crear otro usuario
        User followedUser = new User("followed", "followed@example.com", passwordEncoder.encode("password123"));
        followedUser.setEmailConfirmed(true);
        followedUser = userRepository.save(followedUser);

        // Seguir al usuario
        testUser.getFollowing().add(followedUser);
        userRepository.save(testUser);

        // Crear post del usuario seguido
        Post followedPost = new Post("Followed user post", null, followedUser);
        postRepository.save(followedPost);

        // Obtener feed
        mockMvc.perform(get("/posts/feed")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].description").value("Followed user post"));
    }

    @Test
    void testFeedEmptyWhenNotFollowing() throws Exception {
        mockMvc.perform(get("/posts/feed")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void testCreateCommentOnPrivatePost() throws Exception {
        // Crear usuario privado
        User privateUser = new User("private", "private@example.com", passwordEncoder.encode("password123"));
        privateUser.setPrivate(true);
        privateUser.setEmailConfirmed(true);
        privateUser = userRepository.save(privateUser);

        // Crear post del usuario privado
        Post privatePost = new Post("Private post", null, privateUser);
        privatePost = postRepository.save(privatePost);

        // Intentar comentar sin seguir
        CreateCommentRequest request = new CreateCommentRequest();
        request.setText("Comment");

        mockMvc.perform(post("/posts/" + privatePost.getId() + "/comments")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }
}
