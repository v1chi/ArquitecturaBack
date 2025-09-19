package com.team.socialnetwork.controller;

import com.team.socialnetwork.dto.ChatMessageRequest;
import com.team.socialnetwork.dto.ChatMessageResponse;
import com.team.socialnetwork.entity.Message;
import com.team.socialnetwork.entity.User;
import com.team.socialnetwork.repository.MessageRepository;
import com.team.socialnetwork.repository.UserRepository;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class ChatWebSocketController {

    private final SimpMessagingTemplate messagingTemplate;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;

    public ChatWebSocketController(SimpMessagingTemplate messagingTemplate,
                                   MessageRepository messageRepository,
                                   UserRepository userRepository) {
        this.messagingTemplate = messagingTemplate;
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
    }

    @MessageMapping("/chat.send")
    public void sendMessage(@Payload ChatMessageRequest chatMessage) {
        User sender = userRepository.findById(chatMessage.getSenderId())
                .orElseThrow(() -> new RuntimeException("Sender not found"));
        User receiver = userRepository.findById(chatMessage.getReceiverId())
                .orElseThrow(() -> new RuntimeException("Receiver not found"));

        Message saved = messageRepository.save(new Message(sender, receiver, chatMessage.getContent()));

        messagingTemplate.convertAndSend(
                "/topic/messages/" + receiver.getId(),
                new ChatMessageResponse(
                        saved.getId(),
                        saved.getSender().getId(),
                        saved.getReceiver().getId(),
                        saved.getContent(),
                        saved.getCreatedAt(),
                        saved.isRead()
                )
        );
    }
}
