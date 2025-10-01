package com.team.socialnetwork.controller;

import com.team.socialnetwork.dto.ChatMessageWS;
import com.team.socialnetwork.entity.Message;
import com.team.socialnetwork.entity.User;
import com.team.socialnetwork.repository.MessageRepository;
import com.team.socialnetwork.repository.UserRepository;
import java.security.Principal;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;

@Controller
public class ChatWSController {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public ChatWSController(MessageRepository messageRepository, UserRepository userRepository, SimpMessagingTemplate messagingTemplate) {
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/chat")
    @Transactional
    public void sendMessage(@Payload ChatMessageWS chatMessageWS, Principal principal) {
        User sender = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        User receiver = userRepository.findById(chatMessageWS.getReceiverId())
                .orElseThrow(() -> new RuntimeException("Receptor no encontrado"));

        Message message = new Message(sender, receiver, chatMessageWS.getContent());
        messageRepository.save(message);

        ChatMessageWS response = new ChatMessageWS();
        response.setId(message.getId());
        response.setSenderId(sender.getId());
        response.setReceiverId(receiver.getId());
        response.setContent(message.getContent());
        response.setCreatedAt(message.getCreatedAt());

        messagingTemplate.convertAndSend("/topic/" + receiver.getId(), response);
        messagingTemplate.convertAndSend("/topic/" + sender.getId(), response);

    }
}
