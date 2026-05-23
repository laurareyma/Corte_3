# Seguridad del Sistema

## Mecanismo Implementado: JWT (JSON Web Token)

### ¿Qué es JWT?

JWT es un estándar abierto (RFC 7519) para transmitir información de forma segura
entre partes como un objeto JSON firmado digitalmente.

Un token JWT tiene tres partes separadas por puntos:
```
HEADER.PAYLOAD.SIGNATURE
```

### Flujo de Autenticación

```
1. Cliente → POST /api/auth/login {"usuario": "juan"}
2. riesgo-service → genera token JWT firmado con clave secreta
3. riesgo-service → devuelve {"token": "<JWT>", ...}
4. Cliente → guarda el token
5. Cliente → POST /api/estudiantes/evaluar
             Header: Authorization: Bearer <JWT>
6. riesgo-service → valida el token (JwtFilter)
7. Si válido → procesa la solicitud
8. Si inválido → 403 Forbidden
```

### Endpoints protegidos (requieren JWT)

| Método | Endpoint | Descripción |
|---|---|---|
| POST | /api/estudiantes/evaluar | Evaluar riesgo de un estudiante |
| GET | /api/estudiantes/historial/{nombre} | Consultar historial |

### Endpoints públicos (sin JWT)

| Método | Endpoint | Descripción |
|---|---|---|
| POST | /api/auth/login | Obtener token JWT |
| GET | /api/estudiantes/health | Health check |
| GET | /actuator/** | Métricas y estado |

### Configuración de la clave secreta

En `application.properties`:
```properties
jwt.secret=clave-super-secreta-sabana-2024-debe-ser-larga-minimo-32-chars
```

> ⚠️ En producción esta clave debe estar en variables de entorno o un vault de secretos,
> nunca en el repositorio de código.

### Clases involucradas

- `JwtUtil.java` — Genera, extrae y valida tokens JWT
- `SecurityConfig.java` — Configura Spring Security y registra el filtro JWT
- `AuthController.java` — Expone el endpoint de login para obtener tokens
