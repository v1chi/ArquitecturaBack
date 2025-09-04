package com.ejemplo.mibackend.repository;

import com.ejemplo.mibackend.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    
    // Buscar todos los posts de un usuario específico
    List<Post> findByUsuarioId(Long usuarioId);
    
    // Buscar posts ordenados por fecha de creación (más recientes primero)
    @Query("SELECT p FROM Post p ORDER BY p.fechaCreacion DESC")
    List<Post> findAllOrderByFechaCreacionDesc();
    
    // Buscar posts que contengan cierto texto
    @Query("SELECT p FROM Post p WHERE p.contenido LIKE %:texto% ORDER BY p.fechaCreacion DESC")
    List<Post> findByContenidoContaining(@Param("texto") String texto);
    
    // Contar posts de un usuario
    long countByUsuarioId(Long usuarioId);
}
