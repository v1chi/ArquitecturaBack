package com.ejemplo.mibackend.service;

import com.ejemplo.mibackend.dto.CreatePostRequest;
import com.ejemplo.mibackend.dto.PostResponse;
import com.ejemplo.mibackend.entity.Post;
import com.ejemplo.mibackend.entity.Usuario;
import com.ejemplo.mibackend.repository.PostRepository;
import com.ejemplo.mibackend.repository.UsuarioRepository;
import com.ejemplo.mibackend.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PostService {
    
    @Autowired
    private PostRepository postRepository;
    
    @Autowired
    private UsuarioRepository usuarioRepository;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    public PostResponse createPost(CreatePostRequest request, String token) {
        // Extraer email del token JWT
        String email = jwtUtil.getEmailFromToken(token.replace("Bearer ", ""));
        
        // Buscar usuario
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        // Crear nuevo post
        Post post = new Post();
        post.setContenido(request.getContenido());
        post.setImagen(request.getImagen());
        post.setUsuario(usuario);
        
        // Guardar en la base de datos
        Post postGuardado = postRepository.save(post);
        
        // Convertir a response DTO
        return convertToPostResponse(postGuardado);
    }
    
    public List<PostResponse> getAllPosts() {
        List<Post> posts = postRepository.findAllOrderByFechaCreacionDesc();
        return posts.stream()
                .map(this::convertToPostResponse)
                .collect(Collectors.toList());
    }
    
    public List<PostResponse> getPostsByUser(String token) {
        // Extraer email del token JWT
        String email = jwtUtil.getEmailFromToken(token.replace("Bearer ", ""));
        
        // Buscar usuario
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        List<Post> posts = postRepository.findByUsuarioId(usuario.getId());
        return posts.stream()
                .map(this::convertToPostResponse)
                .collect(Collectors.toList());
    }
    
    public PostResponse updatePost(Long postId, CreatePostRequest request, String token) {
        // Extraer email del token JWT
        String email = jwtUtil.getEmailFromToken(token.replace("Bearer ", ""));
        
        // Buscar usuario
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        // Buscar post
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post no encontrado"));
        
        // Verificar que el post pertenece al usuario
        if (!post.getUsuario().getId().equals(usuario.getId())) {
            throw new RuntimeException("No tienes permisos para editar este post");
        }
        
        // Actualizar campos
        post.setContenido(request.getContenido());
        post.setImagen(request.getImagen());
        post.setFechaActualizacion(LocalDateTime.now());
        
        // Guardar cambios
        Post postActualizado = postRepository.save(post);
        
        return convertToPostResponse(postActualizado);
    }
    
    public void deletePost(Long postId, String token) {
        // Extraer email del token JWT
        String email = jwtUtil.getEmailFromToken(token.replace("Bearer ", ""));
        
        // Buscar usuario
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        // Buscar post
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post no encontrado"));
        
        // Verificar que el post pertenece al usuario
        if (!post.getUsuario().getId().equals(usuario.getId())) {
            throw new RuntimeException("No tienes permisos para eliminar este post");
        }
        
        // Eliminar post
        postRepository.delete(post);
    }
    
    private PostResponse convertToPostResponse(Post post) {
        return new PostResponse(
                post.getId(),
                post.getContenido(),
                post.getImagen(),
                post.getFechaCreacion(),
                post.getFechaActualizacion(),
                post.getUsuario().getId(),
                post.getUsuario().getNombre()
        );
    }
}
