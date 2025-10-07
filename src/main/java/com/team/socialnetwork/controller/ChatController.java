package com.team.socialnetwork.controller;

import com.team.socialnetwork.dto.ChatMessageRequest;
import com.team.socialnetwork.dto.ChatMessageResponse;
import com.team.socialnetwork.dto.ChatUserResponse;
import com.team.socialnetwork.entity.Message;
import com.team.socialnetwork.entity.User;
import com.team.socialnetwork.repository.MessageRepository;
import com.team.socialnetwork.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/messages")
public class ChatController {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;

    public ChatController(MessageRepository messageRepository, UserRepository userRepository) {
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
    }

    @PostMapping("/{receiverId}")
    public ResponseEntity<ChatMessageResponse> sendMessage(
            Authentication authentication,
            @PathVariable Long receiverId,
            @Valid @RequestBody ChatMessageRequest request
    ) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.UNAUTHORIZED, "Missing or invalid token");
        }

        String email = authentication.getName();
        User sender = userRepository.findByEmail(email)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND, "Sender not found"));

        User receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND, "Receiver not found"));

        Message message = new Message(sender, receiver, request.getContent());
        messageRepository.save(message);

        return ResponseEntity.ok(new ChatMessageResponse(
                message.getId(),
                sender.getId(),
                receiver.getId(),
                message.getContent(),
                message.getCreatedAt(),
                message.isRead()
        ));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<List<ChatMessageResponse>> getConversation(
            Authentication authentication,
            @PathVariable Long userId
    ) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.UNAUTHORIZED, "Missing or invalid token");
        }

        String email = authentication.getName();
        User me = userRepository.findByEmail(email)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND, "User not found"));

        User other = userRepository.findById(userId)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND, "User not found"));

        List<Message> messages = messageRepository.findConversation(me.getId(), other.getId());

        List<ChatMessageResponse> resp = messages.stream()
                .map(m -> new ChatMessageResponse(
                        m.getId(),
                        m.getSender().getId(),
                        m.getReceiver().getId(),
                        m.getContent(),
                        m.getCreatedAt(),
                        m.isRead()
                )).toList();

        return ResponseEntity.ok(resp);
    }

    @GetMapping("/chats")
    public ResponseEntity<List<ChatUserResponse>> getMyChats(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.UNAUTHORIZED, "Missing or invalid token");
        }

        String email = authentication.getName();
        User me = userRepository.findByEmail(email)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND, "User not found"));

        List<Long> chatUserIds = messageRepository.findChatUserIds(me.getId());

        List<ChatUserResponse> chats = chatUserIds.stream()
                .map(id -> userRepository.findById(id)
                        .map(user -> new ChatUserResponse(
                                user.getId(), 
                                user.getUsername(), 
                                user.getFullName(), 
                                user.getEmail(), 
                                user.getProfilePicture()
                        ))
                        .orElse(null))
                .filter(Objects::nonNull)
                .toList();

        return ResponseEntity.ok(chats);
    }
}
