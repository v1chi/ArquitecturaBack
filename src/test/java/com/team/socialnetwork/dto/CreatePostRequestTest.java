package com.team.socialnetwork.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("CreatePostRequest DTO Tests")
class CreatePostRequestTest {

    @Test
    @DisplayName("Should create CreatePostRequest with default constructor")
    void shouldCreateCreatePostRequestWithDefaultConstructor() {
        CreatePostRequest request = new CreatePostRequest();

        assertNull(request.getDescription());
        assertNull(request.getImage());
    }

    @Test
    @DisplayName("Should set and get description correctly")
    void shouldSetAndGetDescriptionCorrectly() {
        CreatePostRequest request = new CreatePostRequest();
        String description = "This is a test post description";

        request.setDescription(description);

        assertEquals(description, request.getDescription());
    }

    @Test
    @DisplayName("Should set and get image correctly")
    void shouldSetAndGetImageCorrectly() {
        CreatePostRequest request = new CreatePostRequest();
        String image = "data:image/jpeg;base64,/9j/4AAQSkZJRgABAQEAYABgAAD...";

        request.setImage(image);

        assertEquals(image, request.getImage());
    }

    @Test
    @DisplayName("Should set all properties correctly")
    void shouldSetAllPropertiesCorrectly() {
        CreatePostRequest request = new CreatePostRequest();
        String description = "Test post with image";
        String image = "test-image-data";

        request.setDescription(description);
        request.setImage(image);

        assertEquals(description, request.getDescription());
        assertEquals(image, request.getImage());
    }
}