# Mi Primer Backend en Java

Backend RESTful en Java usando Spring Boot, MySQL y autenticación JWT. Permite registro/login de usuarios, creación y gestión de posts, y validación de seguridad en endpoints protegidos.

## 📋 Requisitos
- Java 17 (o superior)
- Maven
- MySQL
- Visual Studio Code (opcional)

## ⚡ Cómo ejecutar
1. Configura tu base de datos MySQL y actualiza `src/main/resources/application.properties` con tus credenciales.
2. Ejecuta:
   ```bash
   mvn spring-boot:run
   ```
3. Prueba los endpoints con Postman o similar.

## 📁 Estructura del proyecto
```
src/main/java/com/ejemplo/mibackend/
├── MiBackendApplication.java          # Clase principal
├── config/                           # Seguridad y JWT
├── controller/                       # Endpoints REST
├── dto/                              # Objetos de transferencia de datos
├── entity/                           # Modelos de BD (Usuario, Post)
├── repository/                       # Acceso a BD
├── service/                          # Lógica de negocio
├── util/                             # Utilidades (JWT)
```

## 🔒 Autenticación JWT
- Registro y login devuelven un token JWT.
- Endpoints protegidos requieren el header:
  ```
  Authorization: Bearer {tu-token-jwt}
  ```

## 🌐 Endpoints principales
### Usuarios y autenticación
- POST /auth/register — Registro de usuario
- POST /auth/login — Login y obtención de token

### Posts
- GET /posts — Ver todos los posts (público)
- POST /posts — Crear post (protegido)
- GET /posts/my-posts — Ver tus posts (protegido)
- PUT /posts/{id} — Editar tu post (protegido)
- DELETE /posts/{id} — Eliminar tu post (protegido)

## 🛠️ Tecnologías usadas
- Spring Boot
- Spring Security
- JWT (jjwt)
- JPA/Hibernate
- MySQL

## 📚 Explicación rápida
- Controller: Recibe peticiones HTTP
- Service: Lógica de negocio
- Repository: Acceso a la base de datos
- Entity: Modelos/tablas
- Config/Util: Seguridad y JWT

## 💡 Próximos pasos
- Agregar comentarios, likes, perfiles avanzados
- Implementar roles y permisos
- Mejorar validaciones y manejo de errores

---
¡Listo para construir tu propio backend profesional en Java! 🚀