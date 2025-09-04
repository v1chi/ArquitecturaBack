package com.ejemplo.mibackend.controller;

import com.ejemplo.mibackend.dto.CreatePostRequest;
import com.ejemplo.mibackend.dto.PostResponse;
import com.ejemplo.mibackend.service.PostService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/posts")
@CrossOrigin(origins = "*")
public class PostController {
    
    @Autowired
    private PostService postService;
    
    // 🔓 PÚBLICO - Ver todos los posts (sin autenticación)
    @GetMapping
    public ResponseEntity<List<PostResponse>> getAllPosts() {
        try {
            List<PostResponse> posts = postService.getAllPosts();
            return ResponseEntity.ok(posts);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    // 🔐 PROTEGIDO - Crear nuevo post (requiere JWT)
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> createPost(@Valid @RequestBody CreatePostRequest request,
                                       @RequestHeader("Authorization") String authHeader) {
        try {
            PostResponse response = postService.createPost(request, authHeader);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
    
    // 🔐 PROTEGIDO - Ver posts propios (requiere JWT)
    @GetMapping("/my-posts")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getMyPosts(@RequestHeader("Authorization") String authHeader) {
        try {
            List<PostResponse> posts = postService.getPostsByUser(authHeader);
            return ResponseEntity.ok(posts);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
    
    // 🔐 PROTEGIDO - Actualizar post propio (requiere JWT)
    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> updatePost(@PathVariable Long id,
                                       @Valid @RequestBody CreatePostRequest request,
                                       @RequestHeader("Authorization") String authHeader) {
        try {
            PostResponse response = postService.updatePost(id, request, authHeader);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
    
    // 🔐 PROTEGIDO - Eliminar post propio (requiere JWT)
    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> deletePost(@PathVariable Long id,
                                       @RequestHeader("Authorization") String authHeader) {
        try {
            postService.deletePost(id, authHeader);
            return ResponseEntity.ok("Post eliminado exitosamente");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
    
    // 🔓 PÚBLICO - Endpoint de prueba
    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("¡Endpoints de posts funcionando!");
    }
}
