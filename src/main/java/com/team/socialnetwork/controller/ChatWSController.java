package com.team.socialnetwork.controller;

import com.team.socialnetwork.dto.ChatMessageWS;
import com.team.socialnetwork.entity.Message;
import com.team.socialnetwork.entity.User;
import com.team.socialnetwork.repository.MessageRepository;
import com.team.socialnetwork.repository.UserRepository;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

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

    @MessageMapping("/send") // ruta: /app/send
    public void sendMessage(ChatMessageWS chatMessage) {
        // Guardar mensaje en la DB
        User sender = userRepository.findById(chatMessage.getSenderId()).orElseThrow();
        User receiver = userRepository.findById(chatMessage.getReceiverId()).orElseThrow();

        Message message = new Message(sender, receiver, chatMessage.getContent());
        messageRepository.save(message);

        // Enviar mensaje al receptor en tiempo real
        messagingTemplate.convertAndSend("/topic/messages/" + receiver.getId(), chatMessage);
    }
}
