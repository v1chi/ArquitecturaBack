package com.ejemplo.mibackend.service;

import com.ejemplo.mibackend.dto.CreateComentarioRequest;
import com.ejemplo.mibackend.dto.ComentarioResponse;
import com.ejemplo.mibackend.entity.Comentario;
import com.ejemplo.mibackend.entity.Post;
import com.ejemplo.mibackend.entity.Usuario;
import com.ejemplo.mibackend.repository.ComentarioRepository;
import com.ejemplo.mibackend.repository.PostRepository;
import com.ejemplo.mibackend.repository.UsuarioRepository;
import com.ejemplo.mibackend.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ComentarioService {
    
    @Autowired
    private ComentarioRepository comentarioRepository;
    
    @Autowired
    private PostRepository postRepository;
    
    @Autowired
    private UsuarioRepository usuarioRepository;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    public ComentarioResponse crearComentario(Long postId, CreateComentarioRequest request, String token) {
        // Extraer email del token JWT
        String email = jwtUtil.getEmailFromToken(token.replace("Bearer ", ""));
        
        // Buscar usuario
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        // Buscar post
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post no encontrado"));
        
        // Crear comentario
        Comentario comentario = new Comentario(request.getTexto(), usuario, post);
        Comentario comentarioGuardado = comentarioRepository.save(comentario);
        
        return convertToComentarioResponse(comentarioGuardado);
    }
    
    public List<ComentarioResponse> obtenerComentariosPost(Long postId) {
        List<Comentario> comentarios = comentarioRepository.findByPostIdOrderByFechaCreacionAsc(postId);
        return comentarios.stream()
                .map(this::convertToComentarioResponse)
                .collect(Collectors.toList());
    }
    
    public ComentarioResponse actualizarComentario(Long comentarioId, CreateComentarioRequest request, String token) {
        // Extraer email del token JWT
        String email = jwtUtil.getEmailFromToken(token.replace("Bearer ", ""));
        
        // Buscar usuario
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        // Buscar comentario
        Comentario comentario = comentarioRepository.findById(comentarioId)
                .orElseThrow(() -> new RuntimeException("Comentario no encontrado"));
        
        // Verificar que el comentario pertenece al usuario
        if (!comentario.getUsuario().getId().equals(usuario.getId())) {
            throw new RuntimeException("No tienes permisos para editar este comentario");
        }
        
        // Actualizar
        comentario.setTexto(request.getTexto());
        comentario.setFechaActualizacion(LocalDateTime.now());
        
        Comentario comentarioActualizado = comentarioRepository.save(comentario);
        return convertToComentarioResponse(comentarioActualizado);
    }
    
    public void eliminarComentario(Long comentarioId, String token) {
        // Extraer email del token JWT
        String email = jwtUtil.getEmailFromToken(token.replace("Bearer ", ""));
        
        // Buscar usuario
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        // Buscar comentario
        Comentario comentario = comentarioRepository.findById(comentarioId)
                .orElseThrow(() -> new RuntimeException("Comentario no encontrado"));
        
        // Verificar que el comentario pertenece al usuario
        if (!comentario.getUsuario().getId().equals(usuario.getId())) {
            throw new RuntimeException("No tienes permisos para eliminar este comentario");
        }
        
        comentarioRepository.delete(comentario);
    }
    
    public long contarComentarios(Long postId) {
        return comentarioRepository.countByPostId(postId);
    }
    
    private ComentarioResponse convertToComentarioResponse(Comentario comentario) {
        return new ComentarioResponse(
                comentario.getId(),
                comentario.getTexto(),
                comentario.getFechaCreacion(),
                comentario.getFechaActualizacion(),
                comentario.getUsuario().getId(),
                comentario.getUsuario().getNombre()
        );
    }
}
