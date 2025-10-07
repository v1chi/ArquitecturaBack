package com.team.socialnetwork.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("CreateCommentRequest DTO Tests")
class CreateCommentRequestTest {

    @Test
    @DisplayName("Should create CreateCommentRequest with default constructor")
    void shouldCreateCreateCommentRequestWithDefaultConstructor() {
        CreateCommentRequest request = new CreateCommentRequest();

        assertNull(request.getText());
    }

    @Test
    @DisplayName("Should set and get text correctly")
    void shouldSetAndGetTextCorrectly() {
        CreateCommentRequest request = new CreateCommentRequest();
        String text = "This is a test comment";

        request.setText(text);

        assertEquals(text, request.getText());
    }

    @Test
    @DisplayName("Should handle empty text")
    void shouldHandleEmptyText() {
        CreateCommentRequest request = new CreateCommentRequest();
        String emptyText = "";

        request.setText(emptyText);

        assertEquals(emptyText, request.getText());
    }

    @Test
    @DisplayName("Should handle long text")
    void shouldHandleLongText() {
        CreateCommentRequest request = new CreateCommentRequest();
        String longText = "This is a very long comment text that could be used to test the maximum length constraint. ".repeat(10);

        request.setText(longText);

        assertEquals(longText, request.getText());
    }
}