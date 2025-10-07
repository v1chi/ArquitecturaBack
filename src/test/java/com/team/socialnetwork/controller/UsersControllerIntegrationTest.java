package com.team.socialnetwork.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.team.socialnetwork.dto.UpdateProfileRequest;
import com.team.socialnetwork.entity.FollowRequest;
import com.team.socialnetwork.entity.Post;
import com.team.socialnetwork.entity.User;
import com.team.socialnetwork.repository.*;
import com.team.socialnetwork.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class UsersControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private FollowRequestRepository followRequestRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    private String token;
    private User testUser;
    private User targetUser;

    @BeforeEach
    void setUp() {
        // Limpiar datos
        followRequestRepository.deleteAll();
        postRepository.deleteAll();
        userRepository.deleteAll();

        // Crear usuario de prueba
        testUser = new User("testuser", "test@example.com", passwordEncoder.encode("password123"));
        testUser.setFullName("Test User");
        testUser.setEmailConfirmed(true);
        testUser = userRepository.save(testUser);

        // Crear usuario target
        targetUser = new User("targetuser", "target@example.com", passwordEncoder.encode("password123"));
        targetUser.setFullName("Target User");
        targetUser.setEmailConfirmed(true);
        targetUser = userRepository.save(targetUser);

        // Generar token JWT
        Map<String, Object> claims = new HashMap<>();
        token = jwtService.generateAccessToken(testUser.getEmail(), claims);
    }

    @Test
    void testGetUserProfile() throws Exception {
        mockMvc.perform(get("/users/" + targetUser.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("targetuser"))
                .andExpect(jsonPath("$.fullName").value("Target User"));
    }

    @Test
    void testGetUserProfileNotFound() throws Exception {
        mockMvc.perform(get("/users/99999")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void testSearchUsers() throws Exception {
        mockMvc.perform(get("/users/search")
                        .param("query", "target")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].username").value("targetuser"));
    }

    @Test
    void testFollowUser() throws Exception {
        mockMvc.perform(post("/users/" + targetUser.getId() + "/follow")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Followed successfully"));
    }

    @Test
    void testFollowUserAlreadyFollowing() throws Exception {
        // Seguir primero
        testUser.getFollowing().add(targetUser);
        userRepository.save(testUser);

        mockMvc.perform(post("/users/" + targetUser.getId() + "/follow")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isConflict());
    }

    @Test
    void testFollowPrivateUser() throws Exception {
        // Hacer privado al usuario
        targetUser.setPrivate(true);
        userRepository.save(targetUser);

        mockMvc.perform(post("/users/" + targetUser.getId() + "/follow")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Follow request sent"));
    }

    @Test
    void testUnfollowUser() throws Exception {
        // Seguir primero
        testUser.getFollowing().add(targetUser);
        userRepository.save(testUser);

        mockMvc.perform(delete("/users/" + targetUser.getId() + "/follow")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Unfollowed successfully"));
    }

    @Test
    void testUnfollowUserNotFollowing() throws Exception {
        mockMvc.perform(delete("/users/" + targetUser.getId() + "/follow")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isConflict());
    }

    @Test
    void testListFollowers() throws Exception {
        // Hacer que targetUser siga a testUser
        targetUser.getFollowing().add(testUser);
        userRepository.save(targetUser);

        mockMvc.perform(get("/users/" + testUser.getId() + "/followers")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].username").value("targetuser"));
    }

    @Test
    void testListFollowing() throws Exception {
        // testUser sigue a targetUser
        testUser.getFollowing().add(targetUser);
        userRepository.save(testUser);

        mockMvc.perform(get("/users/" + testUser.getId() + "/following")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].username").value("targetuser"));
    }

    @Test
    void testCheckFollowing() throws Exception {
        // Sin seguir
        mockMvc.perform(get("/users/" + targetUser.getId() + "/following/check")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.following").value(false));

        // Seguir
        testUser.getFollowing().add(targetUser);
        userRepository.save(testUser);

        // Con seguimiento
        mockMvc.perform(get("/users/" + targetUser.getId() + "/following/check")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.following").value(true));
    }

    @Test
    void testListUserPosts() throws Exception {
        // Crear un post para targetUser
        Post post = new Post("Test post", null, targetUser);
        postRepository.save(post);

        mockMvc.perform(get("/users/" + targetUser.getId() + "/posts")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].description").value("Test post"));
    }

    @Test
    void testListUserPostsPrivate() throws Exception {
        // Hacer privado al usuario
        targetUser.setPrivate(true);
        userRepository.save(targetUser);

        // Crear post
        Post post = new Post("Private post", null, targetUser);
        postRepository.save(post);

        // Intentar acceder sin seguir
        mockMvc.perform(get("/users/" + targetUser.getId() + "/posts")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void testUpdateProfile() throws Exception {
        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setFullName("Updated Name");

        mockMvc.perform(put("/users/profile")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Profile updated successfully"));
    }



    @Test
    void testUpdateVisibility() throws Exception {
        mockMvc.perform(put("/users/visibility")
                        .param("isPrivate", "true")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Visibility updated successfully"));
    }

    @Test
    void testDeleteAccount() throws Exception {
        mockMvc.perform(delete("/users/account")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Account deleted successfully"));
    }

    @Test
    void testGetFollowRequests() throws Exception {
        // Hacer privado al usuario
        testUser.setPrivate(true);
        userRepository.save(testUser);

        // Crear solicitud de seguimiento
        FollowRequest followRequest = new FollowRequest(targetUser, testUser);
        followRequestRepository.save(followRequest);

        mockMvc.perform(get("/users/follow-requests")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].requester.username").value("targetuser"));
    }

    @Test
    void testAcceptFollowRequest() throws Exception {
        // Hacer privado al usuario
        testUser.setPrivate(true);
        userRepository.save(testUser);

        // Crear solicitud
        FollowRequest followRequest = new FollowRequest(targetUser, testUser);
        followRequest = followRequestRepository.save(followRequest);

        mockMvc.perform(post("/users/follow-requests/" + followRequest.getId() + "/accept")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Follow request accepted"));
    }

    @Test
    void testRejectFollowRequest() throws Exception {
        // Hacer privado al usuario
        testUser.setPrivate(true);
        userRepository.save(testUser);

        // Crear solicitud
        FollowRequest followRequest = new FollowRequest(targetUser, testUser);
        followRequest = followRequestRepository.save(followRequest);

        mockMvc.perform(delete("/users/follow-requests/" + followRequest.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Follow request rejected"));
    }
}
