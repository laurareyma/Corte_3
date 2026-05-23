# Proyecto de Diseño de Software – Corte Dos

## Botón de Pánico Académico — Arquitectura Orientada a Servicios

---

## 📌 Descripción del Sistema

Sistema para la Universidad de La Sabana que permite detectar y atender estudiantes en
riesgo académico mediante un "Botón de Pánico Académico". En este segundo corte el sistema
evoluciona de un monolito Java a una arquitectura orientada a servicios con dos
microservicios independientes, resiliencia, observabilidad y seguridad JWT.

---

## 🏗️ Arquitectura

```
Cliente (curl / Postman)
        │
        │  JWT en header Authorization
        ▼
┌─────────────────────────┐
│     riesgo-service      │  puerto 8080
│  - EvaluadorRiesgoService│
│  - Circuit Breaker       │
│  - Prometheus metrics    │
│  - Jaeger tracing        │
└──────────┬──────────────┘
           │  REST (HTTP)
           ▼
┌─────────────────────────┐
│   notificacion-service  │  puerto 8081
│  - NotificacionService  │
│  - Persistencia H2 (BD  │
│    propia del servicio) │
│  - Prometheus + tracing  │
└─────────────────────────┘

Observabilidad:
  Prometheus  → http://localhost:9090
  Grafana     → http://localhost:3000
  Jaeger      → http://localhost:16686
```

---

## 🚀 Instrucciones para ejecutar

### Requisitos

- Java 17+
- Maven 3.8+
- Docker + Docker Compose

### Paso 1 — Levantar infraestructura de monitoreo

```bash
docker-compose up -d
```

### Paso 2 — Iniciar notificacion-service

```bash
cd notificacion-service
mvn spring-boot:run
```

### Paso 3 — Iniciar riesgo-service (nueva terminal)

```bash
cd riesgo-service
mvn spring-boot:run
```

---

## 🔐 Uso de la API

### Obtener token JWT

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"usuario": "juan"}'
```

Respuesta:

```json
{ "token": "<JWT>", "usuario": "juan", "tipo": "Bearer" }
```

### Evaluar riesgo de un estudiante (requiere JWT)

```bash
curl -X POST http://localhost:8080/api/estudiantes/evaluar \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{"nombre": "Maria", "nivel": "ROJO"}'
```

### Consultar historial (requiere JWT)

```bash
curl -X GET http://localhost:8080/api/estudiantes/historial/Maria \
  -H "Authorization: Bearer <TOKEN>"
```

### Health checks (públicos)

```bash
curl http://localhost:8080/api/estudiantes/health
curl http://localhost:8081/api/notificaciones/health
```

---

## 📊 Observabilidad


| Herramienta | URL                                              | Credenciales     |
| ----------- | ------------------------------------------------ | ---------------- |
| Prometheus  | [http://localhost:9090](http://localhost:9090)   | —                |
| Grafana     | [http://localhost:3000](http://localhost:3000)   | admin / admin123 |
| Jaeger      | [http://localhost:16686](http://localhost:16686) | —                |


### Métricas personalizadas en Prometheus

- `estudiantes_evaluados_total` — Total de estudiantes evaluados
- `alertas_criticas_total` — Total de alertas de nivel ROJO

### Configurar Grafana

1. Ir a [http://localhost:3000](http://localhost:3000) → Connections → Add data source
2. Seleccionar Prometheus → URL: `http://prometheus:9090`
3. Crear dashboard con las métricas anteriores

**Trazas distribuidas:** ambos servicios exportan a OTLP (`otel.exporter.otlp.endpoint`) con el mismo `management.tracing.sampling.probability`. En Jaeger se ve la cadena `riesgo-service` → `notificacion-service`. Los logs incluyen `trace=` y `span=` (Micrometer) para correlacionar con la UI.

---

## 💾 Autonomía y persistencia

- **notificacion-service** mantiene su propio almacén (H2 en archivo bajo `./data/notificaciones`), independiente del servicio de riesgo. Cada servicio tiene su configuración (`spring.application.name`, URL de dependencias, datasource solo donde aplica).
- **riesgo-service** no comparte base de datos con notificaciones; la comunicación es solo por REST, con resiliencia ante caídas del peer.

---

## 🧪 Pruebas

```bash
cd notificacion-service
mvn test

cd ../riesgo-service
mvn test
```

Incluye pruebas de repositorio JPA, controlador web (`@WebMvcTest`), integración del microservicio de notificaciones y pruebas unitarias del evaluador de riesgo con cliente mockeado.

---

## 🛡️ Patrones de Resiliencia

Implementados con **Resilience4j** en `NotificacionClient.java` y timeouts HTTP en `ResilienceConfig.java`:


| Patrón          | Configuración                                                                                |
| --------------- | -------------------------------------------------------------------------------------------- |
| Circuit Breaker | Se abre al 50% de fallos, espera 10s antes de reintentar                                     |
| Retry           | 3 intentos con 2s de espera entre cada uno                                                   |
| TimeLimiter     | 5s por llamada asíncrona (`CompletableFuture`); cancela el futuro en curso si aplica         |
| Timeouts HTTP   | `RestTemplate`: connect 2s, read 5s (`http.client.*-timeout-ms` en `application.properties`) |
| Fallback        | Respuesta local si el servicio de notificaciones falla o excede tiempo                       |


Aplicado en 2 endpoints:

1. `enviarNotificacion()` → POST /api/estudiantes/evaluar
2. `consultarHistorial()` → GET /api/estudiantes/historial/{nombre}

---

## 🔒 Seguridad JWT

- Token generado en `POST /api/auth/login` (público)
- Endpoints protegidos: `/api/estudiantes/evaluar` y `/api/estudiantes/historial/{nombre}`
- Endpoints públicos: `/api/auth/`**, `/api/estudiantes/health`, `/actuator/`**

---

## 👥 Créditos

- **Maria Alejandra Cabrera Arauz** — Product Owner & Análisis de Requerimientos
- **Laura Vanessa Reyes Martinez** — Arquitecta de Software & Diseño
- **Juan Esteban Ramirez Hermosa** — Desarrollador Líder & Análisis Técnico

---

## 📁 Estructura de documentación

```text
README.md
docs/
  arquitectura.md
  resiliencia.md
  observabilidad.md
  seguridad.md
diagramas/
  src/
  services/
  client/
  monitoring/
```

Archivos fuente de diagramas (Mermaid):

- `diagramas/src/arquitectura-general.mmd`
- `diagramas/services/componentes-contenedores.mmd`
- `diagramas/client/interaccion-servicios.mmd`
- `diagramas/monitoring/observabilidad.mmd`

