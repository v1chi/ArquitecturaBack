package com.team.socialnetwork.dto;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

class PostDetailResponseTest {

    @Test
    void testDefaultConstructor() {
        PostDetailResponse response = new PostDetailResponse();
        
        assertNull(response.getId());
        assertNull(response.getCreatedAt());
        assertNull(response.getDescription());
        assertNull(response.getImage());
        assertNull(response.getAuthor());
        assertEquals(0L, response.getLikesCount());
        assertEquals(0L, response.getCommentsCount());
        assertFalse(response.isViewerLiked());
    }

    @Test
    void testParameterizedConstructor() {
        Long id = 1L;
        Instant createdAt = Instant.now();
        String description = "Test description";
        String image = "test-image.jpg";
        SafeUser author = new SafeUser(2L, "John Doe", "johndoe", "john@example.com", Instant.now());
        long likesCount = 10L;
        long commentsCount = 5L;
        boolean viewerLiked = true;

        PostDetailResponse response = new PostDetailResponse(id, createdAt, description, image, 
                                                              author, likesCount, commentsCount, viewerLiked);

        assertEquals(id, response.getId());
        assertEquals(createdAt, response.getCreatedAt());
        assertEquals(description, response.getDescription());
        assertEquals(image, response.getImage());
        assertEquals(author, response.getAuthor());
        assertEquals(likesCount, response.getLikesCount());
        assertEquals(commentsCount, response.getCommentsCount());
        assertTrue(response.isViewerLiked());
    }

    @Test
    void testIdGetterAndSetter() {
        PostDetailResponse response = new PostDetailResponse();
        Long id = 123L;
        
        response.setId(id);
        assertEquals(id, response.getId());
    }

    @Test
    void testCreatedAtGetterAndSetter() {
        PostDetailResponse response = new PostDetailResponse();
        Instant createdAt = Instant.parse("2024-01-01T10:00:00Z");
        
        response.setCreatedAt(createdAt);
        assertEquals(createdAt, response.getCreatedAt());
    }

    @Test
    void testDescriptionGetterAndSetter() {
        PostDetailResponse response = new PostDetailResponse();
        String description = "This is a post description";
        
        response.setDescription(description);
        assertEquals(description, response.getDescription());
    }

    @Test
    void testImageGetterAndSetter() {
        PostDetailResponse response = new PostDetailResponse();
        String image = "profile-pic.png";
        
        response.setImage(image);
        assertEquals(image, response.getImage());
    }

    @Test
    void testAuthorGetterAndSetter() {
        PostDetailResponse response = new PostDetailResponse();
        SafeUser author = new SafeUser(1L, "Jane Doe", "janedoe", "jane@example.com", Instant.now());
        
        response.setAuthor(author);
        assertEquals(author, response.getAuthor());
    }

    @Test
    void testLikesCountGetterAndSetter() {
        PostDetailResponse response = new PostDetailResponse();
        long likesCount = 50L;
        
        response.setLikesCount(likesCount);
        assertEquals(likesCount, response.getLikesCount());
    }

    @Test
    void testCommentsCountGetterAndSetter() {
        PostDetailResponse response = new PostDetailResponse();
        long commentsCount = 25L;
        
        response.setCommentsCount(commentsCount);
        assertEquals(commentsCount, response.getCommentsCount());
    }

    @Test
    void testViewerLikedGetterAndSetter() {
        PostDetailResponse response = new PostDetailResponse();
        
        response.setViewerLiked(true);
        assertTrue(response.isViewerLiked());
        
        response.setViewerLiked(false);
        assertFalse(response.isViewerLiked());
    }

    @Test
    void testWithNullValues() {
        PostDetailResponse response = new PostDetailResponse(null, null, null, null, null, 0L, 0L, false);
        
        assertNull(response.getId());
        assertNull(response.getCreatedAt());
        assertNull(response.getDescription());
        assertNull(response.getImage());
        assertNull(response.getAuthor());
        assertEquals(0L, response.getLikesCount());
        assertEquals(0L, response.getCommentsCount());
        assertFalse(response.isViewerLiked());
    }

    @Test
    void testWithEmptyStrings() {
        PostDetailResponse response = new PostDetailResponse();
        response.setDescription("");
        response.setImage("");
        
        assertEquals("", response.getDescription());
        assertEquals("", response.getImage());
    }
}