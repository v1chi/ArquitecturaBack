package com.ejemplo.mibackend.service;

import com.ejemplo.mibackend.entity.Like;
import com.ejemplo.mibackend.entity.Post;
import com.ejemplo.mibackend.entity.Usuario;
import com.ejemplo.mibackend.repository.LikeRepository;
import com.ejemplo.mibackend.repository.PostRepository;
import com.ejemplo.mibackend.repository.UsuarioRepository;
import com.ejemplo.mibackend.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LikeService {
    
    @Autowired
    private LikeRepository likeRepository;
    
    @Autowired
    private PostRepository postRepository;
    
    @Autowired
    private UsuarioRepository usuarioRepository;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Transactional
    public String toggleLike(Long postId, String token) {
        // Extraer email del token JWT
        String email = jwtUtil.getEmailFromToken(token.replace("Bearer ", ""));
        
        // Buscar usuario
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        // Buscar post
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post no encontrado"));
        
        // Verificar si ya existe el like
        boolean yaExisteLike = likeRepository.existsByUsuarioIdAndPostId(usuario.getId(), postId);
        
        if (yaExisteLike) {
            // Si ya existe, eliminarlo (unlike)
            likeRepository.deleteByUsuarioIdAndPostId(usuario.getId(), postId);
            return "Like eliminado";
        } else {
            // Si no existe, crearlo
            Like nuevoLike = new Like(usuario, post);
            likeRepository.save(nuevoLike);
            return "Like agregado";
        }
    }
    
    public long contarLikes(Long postId) {
        return likeRepository.countByPostId(postId);
    }
    
    public boolean usuarioLikePost(Long usuarioId, Long postId) {
        return likeRepository.existsByUsuarioIdAndPostId(usuarioId, postId);
    }
}
