package com.ejemplo.mibackend.controller;

import com.ejemplo.mibackend.service.LikeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/likes")
public class LikeController {
    
    @Autowired
    private LikeService likeService;
    
    // 🔐 PROTEGIDO - Toggle like/unlike en un post
    @PostMapping("/post/{postId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> toggleLike(@PathVariable Long postId,
                                       @RequestHeader("Authorization") String authHeader) {
        try {
            String mensaje = likeService.toggleLike(postId, authHeader);
            return ResponseEntity.ok().body("{\"message\": \"" + mensaje + "\"}");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }
    
    // 🔓 PÚBLICO - Obtener cantidad de likes de un post
    @GetMapping("/post/{postId}/count")
    public ResponseEntity<?> contarLikes(@PathVariable Long postId) {
        try {
            long cantidadLikes = likeService.contarLikes(postId);
            return ResponseEntity.ok().body("{\"cantidadLikes\": " + cantidadLikes + "}");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }
}
