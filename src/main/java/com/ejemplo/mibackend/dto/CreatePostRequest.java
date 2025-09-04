package com.ejemplo.mibackend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CreatePostRequest {
    
    @NotBlank(message = "El contenido es obligatorio")
    @Size(max = 500, message = "El contenido no puede tener más de 500 caracteres")
    private String contenido;
    
    @Size(max = 255, message = "La URL de la imagen no puede tener más de 255 caracteres")
    private String imagen;
    
    // Constructores
    public CreatePostRequest() {}
    
    public CreatePostRequest(String contenido, String imagen) {
        this.contenido = contenido;
        this.imagen = imagen;
    }
    
    // Getters y Setters
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
}
