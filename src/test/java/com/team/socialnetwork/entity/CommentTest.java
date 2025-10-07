package com.team.socialnetwork.entity;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Comment Entity Tests")
class CommentTest {

    @Test
    @DisplayName("Should create Comment with default constructor")
    void shouldCreateCommentWithDefaultConstructor() {
        Comment comment = new Comment();

        assertNull(comment.getId());
        assertNull(comment.getCreatedAt());
        assertNull(comment.getText());
        assertNull(comment.getPost());
        assertNull(comment.getAuthor());
    }

    @Test
    @DisplayName("Should create Comment with parameterized constructor")
    void shouldCreateCommentWithParameterizedConstructor() {
        User author = new User();
        author.setId(1L);
        author.setUsername("testuser");
        
        Post post = new Post();
        post.setId(1L);
        post.setDescription("Test post");
        
        String text = "Test comment text";

        Comment comment = new Comment(text, post, author);

        assertEquals(text, comment.getText());
        assertEquals(post, comment.getPost());
        assertEquals(author, comment.getAuthor());
    }

    @Test
    @DisplayName("Should set and get all properties correctly")
    void shouldSetAndGetAllPropertiesCorrectly() {
        Comment comment = new Comment();
        Long id = 1L;
        Instant createdAt = Instant.now();
        String text = "Updated comment text";
        
        User author = new User();
        author.setUsername("author");
        
        Post post = new Post();
        post.setDescription("Test post");

        comment.setId(id);
        comment.setCreatedAt(createdAt);
        comment.setText(text);
        comment.setPost(post);
        comment.setAuthor(author);

        assertEquals(id, comment.getId());
        assertEquals(createdAt, comment.getCreatedAt());
        assertEquals(text, comment.getText());
        assertEquals(post, comment.getPost());
        assertEquals(author, comment.getAuthor());
    }

    @Test
    @DisplayName("Should handle text updates correctly")
    void shouldHandleTextUpdatesCorrectly() {
        Comment comment = new Comment();
        String originalText = "Original text";
        String updatedText = "Updated text";

        comment.setText(originalText);
        assertEquals(originalText, comment.getText());

        comment.setText(updatedText);
        assertEquals(updatedText, comment.getText());
    }
}