package com.ejemplo.mibackend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CreateComentarioRequest {
    
    @NotBlank(message = "El comentario no puede estar vacío")
    @Size(max = 500, message = "El comentario no puede tener más de 500 caracteres")
    private String texto;
    
    // Constructores
    public CreateComentarioRequest() {}
    
    public CreateComentarioRequest(String texto) {
        this.texto = texto;
    }
    
    // Getters y Setters
    public String getTexto() {
        return texto;
    }
    
    public void setTexto(String texto) {
        this.texto = texto;
    }
}
