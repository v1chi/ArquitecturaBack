package com.ejemplo.mibackend.repository;

import com.ejemplo.mibackend.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    
    // Buscar usuario por email (para login)
    Optional<Usuario> findByEmail(String email);
    
    // Verificar si existe un usuario con ese email (para registro)
    boolean existsByEmail(String email);
}
