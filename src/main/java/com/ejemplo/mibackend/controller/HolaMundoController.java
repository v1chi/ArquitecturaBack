package com.ejemplo.mibackend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class HolaMundoController {

    @GetMapping("/hola")
    public String holaMundo() {
        return "¡Hola Mundo! Mi primer backend en Java";
    }
    
    @GetMapping("/hola/{nombre}")
    public String holaPersonalizado(@PathVariable String nombre) {
        return "¡Hola " + nombre + "!";
    }
}
