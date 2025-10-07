package com.team.socialnetwork.entity;

import java.time.Instant;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Post Entity Tests")
class PostTest {

    @Test
    @DisplayName("Should create Post with default constructor")
    void shouldCreatePostWithDefaultConstructor() {
        Post post = new Post();

        assertNull(post.getId());
        assertNull(post.getCreatedAt());
        assertNull(post.getDescription());
        assertNull(post.getImage());
        assertNull(post.getAuthor());
        assertNotNull(post.getComments());
        assertNotNull(post.getLikes());
        assertTrue(post.getComments().isEmpty());
        assertTrue(post.getLikes().isEmpty());
    }

    @Test
    @DisplayName("Should create Post with parameterized constructor")
    void shouldCreatePostWithParameterizedConstructor() {
        User author = new User();
        author.setId(1L);
        author.setUsername("testuser");
        
        String description = "Test post description";
        String image = "test-image.jpg";

        Post post = new Post(description, image, author);

        assertEquals(description, post.getDescription());
        assertEquals(image, post.getImage());
        assertEquals(author, post.getAuthor());
        assertNotNull(post.getComments());
        assertNotNull(post.getLikes());
    }

    @Test
    @DisplayName("Should set and get all properties correctly")
    void shouldSetAndGetAllPropertiesCorrectly() {
        Post post = new Post();
        Long id = 1L;
        Instant createdAt = Instant.now();
        String description = "Updated description";
        String image = "updated-image.jpg";
        User author = new User();
        author.setUsername("author");

        post.setId(id);
        post.setCreatedAt(createdAt);
        post.setDescription(description);
        post.setImage(image);
        post.setAuthor(author);

        assertEquals(id, post.getId());
        assertEquals(createdAt, post.getCreatedAt());
        assertEquals(description, post.getDescription());
        assertEquals(image, post.getImage());
        assertEquals(author, post.getAuthor());
    }

    @Test
    @DisplayName("Should handle comments list correctly")
    void shouldHandleCommentsListCorrectly() {
        Post post = new Post();
        ArrayList<Comment> comments = new ArrayList<>();
        
        Comment comment1 = new Comment();
        comment1.setText("First comment");
        Comment comment2 = new Comment();
        comment2.setText("Second comment");
        
        comments.add(comment1);
        comments.add(comment2);

        post.setComments(comments);

        assertEquals(2, post.getComments().size());
        assertEquals(comment1, post.getComments().get(0));
        assertEquals(comment2, post.getComments().get(1));
    }

    @Test
    @DisplayName("Should handle likes list correctly")
    void shouldHandleLikesListCorrectly() {
        Post post = new Post();
        ArrayList<PostLike> likes = new ArrayList<>();
        
        PostLike like1 = new PostLike();
        PostLike like2 = new PostLike();
        
        likes.add(like1);
        likes.add(like2);

        post.setLikes(likes);

        assertEquals(2, post.getLikes().size());
        assertEquals(like1, post.getLikes().get(0));
        assertEquals(like2, post.getLikes().get(1));
    }
}