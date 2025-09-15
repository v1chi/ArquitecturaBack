package com.team.socialnetwork.controller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;

public class ChatController {
    
    @GetMapping("/chat/hello")
    public String hello() {
        return "Hello!";
    }
}
