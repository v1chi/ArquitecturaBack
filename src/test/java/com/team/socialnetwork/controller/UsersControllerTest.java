package com.team.socialnetwork.controller;

import java.util.Arrays;
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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import com.team.socialnetwork.dto.ChangeNameRequest;
import com.team.socialnetwork.dto.ChangePasswordRequest;
import com.team.socialnetwork.dto.ChangeUsernameRequest;
import com.team.socialnetwork.dto.FollowRequestActionRequest;
import com.team.socialnetwork.dto.FollowRequestResponse;
import com.team.socialnetwork.dto.MessageResponse;
import com.team.socialnetwork.dto.PublicUserResponse;
import com.team.socialnetwork.dto.RelationshipResponse;
import com.team.socialnetwork.dto.SafeUser;
import com.team.socialnetwork.dto.UpdateProfilePictureRequest;
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

@ExtendWith(MockitoExtension.class)
class UsersControllerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private PostRepository postRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private CommentLikeRepository commentLikeRepository;

    @Mock
    private FollowRequestRepository followRequestRepository;

    @Mock
    private NotificationService notificationService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private UsersController usersController;

    private User testUser;
    private User targetUser;

    @BeforeEach
    void setUp() {
        testUser = new User("testuser", "test@example.com", "password123");
        testUser.setId(1L);
        testUser.setFullName("Test User");

        targetUser = new User("targetuser", "target@example.com", "pass456");
        targetUser.setId(2L);
        targetUser.setFullName("Target User");
    }

    @Test
    void testGetMe_Success() {
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        ResponseEntity<PublicUserResponse> response = usersController.me(authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("testuser", response.getBody().getUsername());
    }

    @Test
    void testUpdateVisibility_Success() {
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        UpdateVisibilityRequest request = new UpdateVisibilityRequest();
        request.setIsPrivate(true);
        ResponseEntity<MessageResponse> response = usersController.updateVisibility(authentication, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Visibility updated", response.getBody().getMessage());
        verify(userRepository).save(testUser);
    }

    @Test
    void testFollow_PublicUser_Success() {
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(userRepository.findById(2L)).thenReturn(Optional.of(targetUser));

        ResponseEntity<MessageResponse> response = usersController.follow(authentication, 2L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Followed successfully", response.getBody().getMessage());
        verify(userRepository).save(testUser);
    }

    @Test
    void testFollow_PrivateUser_SendsRequest() {
        targetUser.setPrivate(true);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(userRepository.findById(2L)).thenReturn(Optional.of(targetUser));
        when(followRequestRepository.existsByFollowerIdAndTargetId(1L, 2L)).thenReturn(false);

        ResponseEntity<MessageResponse> response = usersController.follow(authentication, 2L);

        assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
        assertEquals("Follow request sent", response.getBody().getMessage());
        verify(followRequestRepository).save(any(FollowRequest.class));
    }

    @Test
    void testFollow_AlreadyFollowing() {
        testUser.setFollowing(new HashSet<>(Arrays.asList(targetUser)));
        
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(userRepository.findById(2L)).thenReturn(Optional.of(targetUser));

        assertThrows(ResponseStatusException.class, () -> 
            usersController.follow(authentication, 2L)
        );
    }

    @Test
    void testFollow_CannotFollowSelf() {
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        assertThrows(ResponseStatusException.class, () -> 
            usersController.follow(authentication, 1L)
        );
    }

    @Test
    void testUnfollow_Success() {
        testUser.setFollowing(new HashSet<>(Arrays.asList(targetUser)));
        
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(userRepository.findById(2L)).thenReturn(Optional.of(targetUser));

        ResponseEntity<MessageResponse> response = usersController.unfollow(authentication, 2L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Unfollowed successfully", response.getBody().getMessage());
        verify(userRepository).save(testUser);
    }

    @Test
    void testUnfollow_CancelPendingRequest() {
        FollowRequest followRequest = new FollowRequest(testUser, targetUser);
        
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(userRepository.findById(2L)).thenReturn(Optional.of(targetUser));
        when(followRequestRepository.findByFollowerIdAndTargetId(1L, 2L)).thenReturn(Optional.of(followRequest));

        ResponseEntity<MessageResponse> response = usersController.unfollow(authentication, 2L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Follow request canceled", response.getBody().getMessage());
        verify(followRequestRepository).delete(followRequest);
    }

    @Test
    void testApproveFollow_Success() {
        FollowRequest followRequest = new FollowRequest(targetUser, testUser);
        
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(userRepository.findById(2L)).thenReturn(Optional.of(targetUser));
        when(followRequestRepository.findByFollowerIdAndTargetId(2L, 1L)).thenReturn(Optional.of(followRequest));

        ResponseEntity<MessageResponse> response = usersController.approveFollow(authentication, 2L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Follow request approved", response.getBody().getMessage());
        verify(followRequestRepository).delete(followRequest);
        verify(userRepository).save(targetUser);
    }

    @Test
    void testRejectFollow_Success() {
        FollowRequest followRequest = new FollowRequest(targetUser, testUser);
        
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(userRepository.findById(2L)).thenReturn(Optional.of(targetUser));
        when(followRequestRepository.findByFollowerIdAndTargetId(2L, 1L)).thenReturn(Optional.of(followRequest));

        ResponseEntity<MessageResponse> response = usersController.rejectFollow(authentication, 2L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Follow request rejected", response.getBody().getMessage());
        verify(followRequestRepository).delete(followRequest);
    }

    @Test
    void testRemoveFollower_Success() {
        targetUser.setFollowing(new HashSet<>(Arrays.asList(testUser)));
        testUser.setFollowers(new HashSet<>(Arrays.asList(targetUser)));
        
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(userRepository.findById(2L)).thenReturn(Optional.of(targetUser));

        ResponseEntity<MessageResponse> response = usersController.removeFollower(authentication, 2L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Follower removed successfully", response.getBody().getMessage());
    }

    @Test
    void testListUsers_StartsWith() {
        when(userRepository.searchByPrefix("test")).thenReturn(Arrays.asList(testUser));

        ResponseEntity<List<SafeUser>> response = usersController.listUsers("test", "startsWith");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void testListUsers_Contains() {
        when(userRepository.searchByTerm("test")).thenReturn(Arrays.asList(testUser));

        ResponseEntity<List<SafeUser>> response = usersController.listUsers("test", "contains");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void testGetUserById_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        ResponseEntity<PublicUserResponse> response = usersController.getUserById(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("testuser", response.getBody().getUsername());
    }

    @Test
    void testListFollowers_Success() {
        testUser.setFollowers(new HashSet<>(Arrays.asList(targetUser)));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        ResponseEntity<List<SafeUser>> response = usersController.listFollowers(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void testListFollowing_Success() {
        testUser.setFollowing(new HashSet<>(Arrays.asList(targetUser)));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        ResponseEntity<List<SafeUser>> response = usersController.listFollowing(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void testRelationship_Success() {
        testUser.setFollowing(new HashSet<>(Arrays.asList(targetUser)));
        
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(userRepository.findById(2L)).thenReturn(Optional.of(targetUser));
        when(followRequestRepository.existsByFollowerIdAndTargetId(1L, 2L)).thenReturn(false);

        ResponseEntity<RelationshipResponse> response = usersController.relationship(authentication, 2L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isFollowing());
    }

    @Test
    void testCountFollowers_Success() {
        testUser.setFollowers(new HashSet<>(Arrays.asList(targetUser)));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        ResponseEntity<Map<String, Long>> response = usersController.countFollowers(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1L, response.getBody().get("count"));
    }

    @Test
    void testCountFollowing_Success() {
        testUser.setFollowing(new HashSet<>(Arrays.asList(targetUser)));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        ResponseEntity<Map<String, Long>> response = usersController.countFollowing(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1L, response.getBody().get("count"));
    }

    @Test
    void testListUserPosts_Success() {
        Post post = new Post("Test", "img.jpg", testUser);
        
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(postRepository.findByAuthorId(1L)).thenReturn(Arrays.asList(post));

        ResponseEntity<?> response = usersController.listUserPosts(authentication, 1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testChangePassword_Success() {
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("oldPassword", "password123")).thenReturn(true);
        when(passwordEncoder.matches("newPassword", "password123")).thenReturn(false);
        when(passwordEncoder.encode("newPassword")).thenReturn("encodedNewPassword");

        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword("oldPassword");
        request.setNewPassword("newPassword");
        
        ResponseEntity<MessageResponse> response = usersController.changePassword(authentication, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Password updated successfully", response.getBody().getMessage());
        verify(userRepository).save(testUser);
    }

    @Test
    void testChangeUsername_Success() {
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(userRepository.existsByUsername("newusername")).thenReturn(false);

        ChangeUsernameRequest request = new ChangeUsernameRequest();
        request.setUsername("newusername");
        
        ResponseEntity<MessageResponse> response = usersController.changeUsername(authentication, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Username updated successfully", response.getBody().getMessage());
        verify(userRepository).save(testUser);
    }

    @Test
    void testChangeUsername_AlreadyExists() {
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(userRepository.existsByUsername("existinguser")).thenReturn(true);

        ChangeUsernameRequest request = new ChangeUsernameRequest();
        request.setUsername("existinguser");
        
        assertThrows(ResponseStatusException.class, () -> 
            usersController.changeUsername(authentication, request)
        );
    }

    @Test
    void testChangeFullName_Success() {
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        ChangeNameRequest request = new ChangeNameRequest();
        request.setName("New Full Name");
        
        ResponseEntity<MessageResponse> response = usersController.changeFullName(authentication, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Full name updated successfully", response.getBody().getMessage());
        verify(userRepository).save(testUser);
    }

    @Test
    void testDeleteAccount_Success() {
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        ResponseEntity<MessageResponse> response = usersController.deleteAccount(authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Account deleted successfully", response.getBody().getMessage());
        verify(commentLikeRepository).deleteByUserId(1L);
        verify(userRepository).delete(testUser);
    }

    @Test
    void testUpdateProfile_Success() {
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setFullName("Updated Name");
        
        ResponseEntity<MessageResponse> response = usersController.updateProfile(authentication, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Profile updated successfully", response.getBody().getMessage());
        verify(userRepository).save(testUser);
    }

    @Test
    void testGetPendingFollowRequests_PrivateUser() {
        testUser.setPrivate(true);
        FollowRequest followRequest = new FollowRequest(targetUser, testUser);
        
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(followRequestRepository.findByTargetId(1L)).thenReturn(Arrays.asList(followRequest));

        ResponseEntity<List<FollowRequestResponse>> response = usersController.getPendingFollowRequests(authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void testGetPendingFollowRequests_PublicUser() {
        testUser.setPrivate(false);
        
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        ResponseEntity<List<FollowRequestResponse>> response = usersController.getPendingFollowRequests(authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isEmpty());
    }

    @Test
    void testHandleFollowRequest_Accept() {
        FollowRequest followRequest = new FollowRequest(targetUser, testUser);
        followRequest.setId(1L);
        
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(followRequestRepository.findById(1L)).thenReturn(Optional.of(followRequest));

        FollowRequestActionRequest actionRequest = new FollowRequestActionRequest();
        actionRequest.setAction("accept");
        
        ResponseEntity<MessageResponse> response = usersController.handleFollowRequest(authentication, 1L, actionRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Follow request accepted", response.getBody().getMessage());
        verify(followRequestRepository).delete(followRequest);
    }

    @Test
    void testHandleFollowRequest_Reject() {
        FollowRequest followRequest = new FollowRequest(targetUser, testUser);
        followRequest.setId(1L);
        
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(followRequestRepository.findById(1L)).thenReturn(Optional.of(followRequest));

        FollowRequestActionRequest actionRequest = new FollowRequestActionRequest();
        actionRequest.setAction("reject");
        
        ResponseEntity<MessageResponse> response = usersController.handleFollowRequest(authentication, 1L, actionRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Follow request rejected", response.getBody().getMessage());
        verify(followRequestRepository).delete(followRequest);
    }

    @Test
    void testUpdateProfilePicture_Success() {
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        UpdateProfilePictureRequest request = new UpdateProfilePictureRequest();
        request.setProfilePicture("data:image/png;base64,abc123");
        
        ResponseEntity<MessageResponse> response = usersController.updateProfilePicture(authentication, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Profile picture updated successfully", response.getBody().getMessage());
        verify(userRepository).save(testUser);
    }
}
