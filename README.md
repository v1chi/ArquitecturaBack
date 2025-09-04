# Mi Primer Backend en Java

Estructura básica de un backend en Java con Spring Boot para empezar a trabajar.

## 📋 Lo que necesitas tener instalado

- **Java 17** (o superior)
- **Maven**
- **Visual Studio Code** con extensiones de Java

## 🚀 Cómo ejecutar

1. **Ejecutar la aplicación:**
   ```bash
   mvn spring-boot:run
   ```

2. **Probar en el navegador:**
   - `http://localhost:8080/api/hola`
   - `http://localhost:8080/api/hola/TuNombre`

## 📁 Estructura básica

```
src/main/java/com/ejemplo/mibackend/
├── MiBackendApplication.java          # Clase principal
└── controller/
    └── HolaMundoController.java       # Tu primer controlador REST
```

## ✅ Lo que tienes listo

- ✅ Proyecto Spring Boot funcionando
- ✅ Un endpoint básico de prueba
- ✅ Estructura organizada para crecer

## � Próximos pasos cuando quieras agregar más

- Agregar base de datos (H2, MySQL, PostgreSQL)
- Crear más controladores
- Añadir servicios y repositorios
- Implementar validaciones