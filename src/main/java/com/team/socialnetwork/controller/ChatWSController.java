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
        System.out.println("Mensaje recibido en backend: " + chatMessageWS);
        System.out.println("Usuario logueado: " + principal.getName());
        // 1. Obtener el usuario que envía desde el JWT
        User sender = userRepository.findByEmail(principal.getName())
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // 2. Obtener receptor
        User receiver = userRepository.findById(chatMessageWS.getReceiverId())
            .orElseThrow(() -> new RuntimeException("Receptor no encontrado"));

        // Crear y guardar mensaje 
        Message message = new Message(sender, receiver, chatMessageWS.getContent());
        messageRepository.save(message);

        // Enviar mensaje al topic del receptor 
        ChatMessageWS response = new ChatMessageWS();
        response.setId(message.getId());  // nuevo: asignar id generado
        response.setSenderId(sender.getId());
        response.setReceiverId(receiver.getId());
        response.setContent(message.getContent());
        response.setCreatedAt(message.getCreatedAt()); // nuevo: asignar fecha
        messagingTemplate.convertAndSend("/topic/" + receiver.getId(), response);
        messagingTemplate.convertAndSend("/topic/" + sender.getId(), response); // nuevo: enviar también al emisor

    }
}
