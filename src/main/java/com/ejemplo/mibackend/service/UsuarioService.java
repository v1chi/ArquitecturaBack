package com.ejemplo.mibackend.service;

import com.ejemplo.mibackend.entity.Usuario;
import com.ejemplo.mibackend.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class UsuarioService {
    
    @Autowired
    private UsuarioRepository usuarioRepository;
    
    // Obtener todos los usuarios
    public List<Usuario> obtenerTodos() {
        return usuarioRepository.findAll();
    }
    
    // Crear usuario
    public Usuario crear(Usuario usuario) {
        return usuarioRepository.save(usuario);
    }
    
    // Buscar por ID
    public Optional<Usuario> obtenerPorId(Long id) {
        return usuarioRepository.findById(id);
    }
    
    // Buscar por email
    public Optional<Usuario> obtenerPorEmail(String email) {
        return usuarioRepository.findByEmail(email);
    }
    
    // Eliminar usuario
    public void eliminar(Long id) {
        usuarioRepository.deleteById(id);
    }
}
