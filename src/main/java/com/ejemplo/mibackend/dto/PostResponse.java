package com.ejemplo.mibackend.dto;

import java.time.LocalDateTime;

public class PostResponse {
    
    private Long id;
    private String contenido;
    private String descripcion;
    private String imagen;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
    private Long usuarioId;
    private String nombreUsuario;
    private int cantidadLikes;
    private int cantidadComentarios;
    private boolean likedByCurrentUser; // Para saber si el usuario actual le dio like
    
    // Constructores
    public PostResponse() {}
    
    public PostResponse(Long id, String contenido, String descripcion, String imagen, 
                       LocalDateTime fechaCreacion, LocalDateTime fechaActualizacion, 
                       Long usuarioId, String nombreUsuario, int cantidadLikes, 
                       int cantidadComentarios, boolean likedByCurrentUser) {
        this.id = id;
        this.contenido = contenido;
        this.descripcion = descripcion;
        this.imagen = imagen;
        this.fechaCreacion = fechaCreacion;
        this.fechaActualizacion = fechaActualizacion;
        this.usuarioId = usuarioId;
        this.nombreUsuario = nombreUsuario;
        this.cantidadLikes = cantidadLikes;
        this.cantidadComentarios = cantidadComentarios;
        this.likedByCurrentUser = likedByCurrentUser;
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
    
    public String getDescripcion() {
        return descripcion;
    }
    
    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
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
    
    public int getCantidadLikes() {
        return cantidadLikes;
    }
    
    public void setCantidadLikes(int cantidadLikes) {
        this.cantidadLikes = cantidadLikes;
    }
    
    public int getCantidadComentarios() {
        return cantidadComentarios;
    }
    
    public void setCantidadComentarios(int cantidadComentarios) {
        this.cantidadComentarios = cantidadComentarios;
    }
    
    public boolean isLikedByCurrentUser() {
        return likedByCurrentUser;
    }
    
    public void setLikedByCurrentUser(boolean likedByCurrentUser) {
        this.likedByCurrentUser = likedByCurrentUser;
    }
}
