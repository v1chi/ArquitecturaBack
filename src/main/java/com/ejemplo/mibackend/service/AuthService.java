package com.ejemplo.mibackend.service;

import com.ejemplo.mibackend.dto.AuthResponse;
import com.ejemplo.mibackend.dto.LoginRequest;
import com.ejemplo.mibackend.dto.RegisterRequest;
import com.ejemplo.mibackend.entity.Usuario;
import com.ejemplo.mibackend.repository.UsuarioRepository;
import com.ejemplo.mibackend.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    private UsuarioRepository usuarioRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    public AuthResponse register(RegisterRequest request) {
        // Verificar si el email ya existe
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("El email ya está registrado");
        }
        
        // Validar que las contraseñas coincidan
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("Las contraseñas no coinciden");
        }
        
        // Crear nuevo usuario
        Usuario usuario = new Usuario();
        usuario.setNombre(request.getNombre());
        usuario.setEmail(request.getEmail());
        usuario.setPassword(passwordEncoder.encode(request.getPassword())); // Hashear la contraseña
        usuario.setBio(null); // Bio opcional, se puede agregar después
        usuario.setFotoPerfil(request.getFotoPerfil()); // Foto opcional
        
        // Guardar en la base de datos
        Usuario usuarioGuardado = usuarioRepository.save(usuario);
        
        // Generar token JWT
        String token = jwtUtil.generateToken(usuarioGuardado.getEmail(), usuarioGuardado.getId());
        
        return new AuthResponse(token, usuarioGuardado.getId(), usuarioGuardado.getNombre(), usuarioGuardado.getEmail());
    }
    
    public AuthResponse login(LoginRequest request) {
        // Buscar usuario por email
        Usuario usuario = usuarioRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new RuntimeException("Credenciales inválidas"));
        
        // Verificar contraseña
        if (!passwordEncoder.matches(request.getPassword(), usuario.getPassword())) {
            throw new RuntimeException("Credenciales inválidas");
        }
        
        // Generar token JWT
        String token = jwtUtil.generateToken(usuario.getEmail(), usuario.getId());
        
        return new AuthResponse(token, usuario.getId(), usuario.getNombre(), usuario.getEmail());
    }
}
