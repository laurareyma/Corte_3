# Resiliencia del Sistema

## Patrón Implementado: Circuit Breaker + Retry + Fallback

### ¿Qué es el Circuit Breaker?

El Circuit Breaker (Interruptor de Circuito) es un patrón de resiliencia que evita que
un servicio siga intentando llamar a otro servicio que está fallando, protegiendo así
al sistema de cascadas de errores.

Estados del Circuit Breaker:
- **CLOSED**: Todo funciona. Las llamadas pasan normalmente.
- **OPEN**: Se detectaron demasiados fallos. Las llamadas se rechazan inmediatamente (sin intentar).
- **HALF_OPEN**: Periodo de prueba. Se permiten algunas llamadas para ver si el servicio se recuperó.

### ¿Qué es Retry?

El patrón Retry reintenta automáticamente una operación fallida un número determinado
de veces antes de considerar que hay un error definitivo.

### ¿Qué es Fallback?

El Fallback es la respuesta alternativa que se devuelve cuando el Circuit Breaker está
abierto o cuando se agotan los reintentos. Garantiza que el sistema siga funcionando
de forma degradada en lugar de fallar completamente.

## Configuración Aplicada

```properties
# Circuit Breaker
resilience4j.circuitbreaker.instances.notificacionService.failure-rate-threshold=50
resilience4j.circuitbreaker.instances.notificacionService.wait-duration-in-open-state=10s
resilience4j.circuitbreaker.instances.notificacionService.sliding-window-size=5

# Retry
resilience4j.retry.instances.notificacionService.max-attempts=3
resilience4j.retry.instances.notificacionService.wait-duration=2s

# Timeout
resilience4j.timelimiter.instances.notificacionService.timeout-duration=5s
```

## Endpoints donde se aplica

1. `POST /api/estudiantes/evaluar` → llama a `enviarNotificacion()` con Circuit Breaker + Retry
2. `GET /api/estudiantes/historial/{nombre}` → llama a `consultarHistorial()` con Circuit Breaker + Retry

## Fallback implementado

Si el notificacion-service no está disponible:
- `enviarNotificacion` devuelve: "Notificación no disponible temporalmente. Riesgo registrado localmente."
- `consultarHistorial` devuelve: `[]` (historial vacío)
