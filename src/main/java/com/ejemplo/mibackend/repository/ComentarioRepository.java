package com.ejemplo.mibackend.repository;

import com.ejemplo.mibackend.entity.Comentario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ComentarioRepository extends JpaRepository<Comentario, Long> {
    
    // Buscar comentarios de un post específico ordenados por fecha
    @Query("SELECT c FROM Comentario c WHERE c.post.id = :postId ORDER BY c.fechaCreacion ASC")
    List<Comentario> findByPostIdOrderByFechaCreacionAsc(@Param("postId") Long postId);
    
    // Contar comentarios de un post
    long countByPostId(Long postId);
    
    // Buscar comentarios de un usuario específico
    @Query("SELECT c FROM Comentario c WHERE c.usuario.id = :usuarioId ORDER BY c.fechaCreacion DESC")
    List<Comentario> findByUsuarioIdOrderByFechaCreacionDesc(@Param("usuarioId") Long usuarioId);
    
    // Buscar comentarios de un post con paginación (los más recientes primero)
    @Query("SELECT c FROM Comentario c WHERE c.post.id = :postId ORDER BY c.fechaCreacion DESC")
    List<Comentario> findByPostIdOrderByFechaCreacionDesc(@Param("postId") Long postId);
}
