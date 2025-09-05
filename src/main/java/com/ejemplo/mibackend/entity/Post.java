package com.ejemplo.mibackend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import com.fasterxml.jackson.annotation.JsonBackReference;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "posts")
public class Post {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "El contenido es obligatorio")
    @Size(max = 500, message = "El contenido no puede tener más de 500 caracteres")
    @Column(columnDefinition = "TEXT")
    private String contenido;
    
    @Size(max = 1000, message = "La descripción no puede tener más de 1000 caracteres")
    @Column(columnDefinition = "TEXT")
    private String descripcion;
    
    @Size(max = 255, message = "La URL de la imagen no puede tener más de 255 caracteres")
    private String imagen;
    
    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;
    
    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;
    
    // Relación con Usuario - muchos posts pertenecen a un usuario
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    @JsonBackReference
    private Usuario usuario;
    
    // Relación con Likes - un post puede tener muchos likes
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Like> likes = new ArrayList<>();
    
    // Relación con Comentarios - un post puede tener muchos comentarios
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comentario> comentarios = new ArrayList<>();
    
    // Constructores
    public Post() {
        this.fechaCreacion = LocalDateTime.now();
    }
    
    public Post(String contenido, String imagen, Usuario usuario) {
        this();
        this.contenido = contenido;
        this.imagen = imagen;
        this.usuario = usuario;
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
    
    public Usuario getUsuario() {
        return usuario;
    }
    
    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }
    
    public List<Like> getLikes() {
        return likes;
    }
    
    public void setLikes(List<Like> likes) {
        this.likes = likes;
    }
    
    public List<Comentario> getComentarios() {
        return comentarios;
    }
    
    public void setComentarios(List<Comentario> comentarios) {
        this.comentarios = comentarios;
    }
    
    // Métodos útiles para contar likes y comentarios
    public int getCantidadLikes() {
        return likes != null ? likes.size() : 0;
    }
    
    public int getCantidadComentarios() {
        return comentarios != null ? comentarios.size() : 0;
    }
    
    @PreUpdate
    protected void onUpdate() {
        fechaActualizacion = LocalDateTime.now();
    }
}
