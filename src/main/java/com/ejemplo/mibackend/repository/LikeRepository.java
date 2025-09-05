package com.ejemplo.mibackend.repository;

import com.ejemplo.mibackend.entity.Like;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LikeRepository extends JpaRepository<Like, Long> {
    
    // Buscar like específico de un usuario en un post
    Optional<Like> findByUsuarioIdAndPostId(Long usuarioId, Long postId);
    
    // Contar likes de un post
    long countByPostId(Long postId);
    
    // Verificar si un usuario ya le dio like a un post
    boolean existsByUsuarioIdAndPostId(Long usuarioId, Long postId);
    
    // Eliminar like específico
    void deleteByUsuarioIdAndPostId(Long usuarioId, Long postId);
    
    // Obtener todos los likes de un usuario
    @Query("SELECT l FROM Like l WHERE l.usuario.id = :usuarioId ORDER BY l.fechaCreacion DESC")
    java.util.List<Like> findByUsuarioIdOrderByFechaCreacionDesc(@Param("usuarioId") Long usuarioId);
}
