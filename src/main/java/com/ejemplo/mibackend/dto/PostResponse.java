package com.ejemplo.mibackend.dto;

import java.time.LocalDateTime;

public class PostResponse {
    
    private Long id;
    private String contenido;
    private String imagen;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
    private Long usuarioId;
    private String nombreUsuario;
    
    // Constructores
    public PostResponse() {}
    
    public PostResponse(Long id, String contenido, String imagen, LocalDateTime fechaCreacion, 
                       LocalDateTime fechaActualizacion, Long usuarioId, String nombreUsuario) {
        this.id = id;
        this.contenido = contenido;
        this.imagen = imagen;
        this.fechaCreacion = fechaCreacion;
        this.fechaActualizacion = fechaActualizacion;
        this.usuarioId = usuarioId;
        this.nombreUsuario = nombreUsuario;
    }
    
    // Getters y Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getContenido() {
        return contenido;
    }
    
    public void setContenido(String contenido) {
        this.contenido = contenido;
    }
    
    public String getImagen() {
        return imagen;
    }
    
    public void setImagen(String imagen) {
        this.imagen = imagen;
    }
    
    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }
    
    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }
    
    public LocalDateTime getFechaActualizacion() {
        return fechaActualizacion;
    }
    
    public void setFechaActualizacion(LocalDateTime fechaActualizacion) {
        this.fechaActualizacion = fechaActualizacion;
    }
    
    public Long getUsuarioId() {
        return usuarioId;
    }
    
    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }
    
    public String getNombreUsuario() {
        return nombreUsuario;
    }
    
    public void setNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
    }
}
