package com.ejemplo.mibackend.controller;

import com.ejemplo.mibackend.dto.CreateComentarioRequest;
import com.ejemplo.mibackend.dto.ComentarioResponse;
import com.ejemplo.mibackend.service.ComentarioService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/comentarios")
public class ComentarioController {
    
    @Autowired
    private ComentarioService comentarioService;
    
    // 🔐 PROTEGIDO - Crear comentario en un post
    @PostMapping("/post/{postId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> crearComentario(@PathVariable Long postId,
                                           @Valid @RequestBody CreateComentarioRequest request,
                                           @RequestHeader("Authorization") String authHeader) {
        try {
            ComentarioResponse response = comentarioService.crearComentario(postId, request, authHeader);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
    
    // 🔓 PÚBLICO - Obtener comentarios de un post
    @GetMapping("/post/{postId}")
    public ResponseEntity<?> obtenerComentarios(@PathVariable Long postId) {
        try {
            List<ComentarioResponse> comentarios = comentarioService.obtenerComentariosPost(postId);
            return ResponseEntity.ok(comentarios);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
    
    // 🔐 PROTEGIDO - Actualizar comentario propio
    @PutMapping("/{comentarioId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> actualizarComentario(@PathVariable Long comentarioId,
                                                @Valid @RequestBody CreateComentarioRequest request,
                                                @RequestHeader("Authorization") String authHeader) {
        try {
            ComentarioResponse response = comentarioService.actualizarComentario(comentarioId, request, authHeader);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
    
    // 🔐 PROTEGIDO - Eliminar comentario propio
    @DeleteMapping("/{comentarioId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> eliminarComentario(@PathVariable Long comentarioId,
                                              @RequestHeader("Authorization") String authHeader) {
        try {
            comentarioService.eliminarComentario(comentarioId, authHeader);
            return ResponseEntity.ok("Comentario eliminado exitosamente");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
    
    // 🔓 PÚBLICO - Contar comentarios de un post
    @GetMapping("/post/{postId}/count")
    public ResponseEntity<?> contarComentarios(@PathVariable Long postId) {
        try {
            long cantidadComentarios = comentarioService.contarComentarios(postId);
            return ResponseEntity.ok().body("{\"cantidadComentarios\": " + cantidadComentarios + "}");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
}
