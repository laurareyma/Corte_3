# Modelo C4 — Botón de Pánico Académico (Corte 3)

**Autores:** Maria Alejandra Cabrera Arauz · Laura Vanessa Reyes Martinez · Juan Esteban Ramirez Hermosa  
**Curso:** Diseño y Arquitectura de Software  
**Profesor:** César Augusto Vega Fernández

---

El modelo C4 (Simon Brown) describe la arquitectura en cuatro niveles de abstracción:
**Contexto → Contenedores → Componentes → Código**.
Cada nivel hace zoom sobre el interior del anterior.

---

## Nivel 1 — Diagrama de Contexto (System Context)

**Pregunta:** ¿Qué hace el sistema y con quién interactúa?

```mermaid
flowchart TB
    classDef person fill:#08427b,color:#fff,stroke:#052e56
    classDef system fill:#1168bd,color:#fff,stroke:#0b4884
    classDef external fill:#999,color:#fff,stroke:#6b6b6b

    DOCENTE["👩‍🏫 Docente / Sistema Académico\n[Persona / Sistema externo]\nEnvía solicitudes de evaluación\nde riesgo estudiantil"]:::person

    ADMIN["👨‍💼 Administrador TI\n[Persona]\nMonitorea la salud y\nmétricas del sistema"]:::person

    SISTEMA["🎓 Botón de Pánico Académico\n[Sistema de Software]\nDetecta estudiantes en riesgo\ny gestiona notificaciones\ny apoyos académicos/psicológicos"]:::system

    MONITOR["📊 Stack de Observabilidad\n[Sistema externo]\nPrometheus · Grafana · Jaeger"]:::external

    SONAR["🔍 SonarCloud\n[Sistema externo]\nAnálisis de calidad\ny cobertura de código"]:::external

    DOCENTE -->|"REST/JWT\nEvalúa riesgo, consulta historial"| SISTEMA
    ADMIN -->|"HTTP\nConsulta métricas y trazas"| SISTEMA
    SISTEMA -->|"OTLP / Prometheus scrape\nMétricas y trazas distribuidas"| MONITOR
    ADMIN -->|"HTTPS\nRevisa quality gate"| SONAR
```

**Relaciones clave:**
- El **Docente/Sistema académico** es el actor principal: autentica con JWT y evalúa estudiantes.
- El **Administrador TI** monitorea salud via Grafana y Jaeger.
- El sistema exporta métricas y trazas de forma pasiva al stack de observabilidad.

---

## Nivel 2 — Diagrama de Contenedores (Container Diagram)

**Pregunta:** ¿Cuáles son los principales ejecutables/servicios y cómo se comunican?

```mermaid
flowchart TB
    classDef person fill:#08427b,color:#fff,stroke:#052e56
    classDef container fill:#1168bd,color:#fff,stroke:#0b4884
    classDef database fill:#23652c,color:#fff,stroke:#164d20
    classDef monitoring fill:#7d5a3c,color:#fff,stroke:#5c3e28
    classDef infra fill:#555,color:#fff,stroke:#333

    ACTOR["👩‍🏫 Cliente\n(curl / Postman / Newman)"]:::person

    subgraph SISTEMA["Sistema — Botón de Pánico Académico"]
        RS["⚙️ riesgo-service\n[Spring Boot 3 · Java 17]\nPuerto :8080\n\nAutentica usuarios (JWT)\nEvalúa nivel de riesgo\nAplica patrones Factory y Observer\nGestiona resiliencia (Circuit Breaker)"]:::container

        NS["⚙️ notificacion-service\n[Spring Boot 3 · Java 17]\nPuerto :8081\n\nRecibe notificaciones de alerta\nPersiste historial de eventos\nExpone historial por estudiante"]:::container

        DB[("🗄️ Base de datos H2\n[Archivo local]\n./data/notificaciones\n\nHistorial de notificaciones")]:::database
    end

    subgraph OBS["Stack de Observabilidad (Docker)"]
        PROM["📈 Prometheus\n[Docker :9090]\nRecolecta métricas cada 15s"]:::monitoring
        GRAF["📊 Grafana\n[Docker :3000]\nDashboards de métricas"]:::monitoring
        JAE["🔍 Jaeger\n[Docker :16686 / :4318]\nTrazas distribuidas OTLP"]:::monitoring
    end

    subgraph K8S["Kubernetes (Producción)"]
        RBAC_K["RBAC + NetworkPolicy\n+ Secrets"]:::infra
    end

    ACTOR -->|"POST /api/auth/login\n[HTTP/JSON]"| RS
    ACTOR -->|"POST /api/estudiantes/evaluar\n[HTTP/JSON + JWT]"| RS
    ACTOR -->|"GET /api/estudiantes/historial\n[HTTP + JWT]"| RS

    RS -->|"POST /api/notificaciones/enviar\n[REST interno · Circuit Breaker]"| NS
    RS -->|"GET /api/notificaciones/historial\n[REST interno · Circuit Breaker]"| NS
    NS --> DB

    RS -->|"Prometheus scrape\n/actuator/prometheus"| PROM
    NS -->|"Prometheus scrape\n/actuator/prometheus"| PROM
    PROM --> GRAF
    RS -->|"Trazas OTLP\nHTTP :4318"| JAE
    NS -->|"Trazas OTLP\nHTTP :4318"| JAE

    RS -.->|"Desplegado en"| K8S
    NS -.->|"Desplegado en"| K8S
```

**Tecnología por contenedor:**

| Contenedor | Tecnología | Puerto | Protocolo |
|-----------|-----------|--------|-----------|
| riesgo-service | Spring Boot 3 / Java 17 | 8080 | HTTP/REST |
| notificacion-service | Spring Boot 3 / Java 17 / H2 | 8081 | HTTP/REST |
| Prometheus | Docker image `prom/prometheus` | 9090 | HTTP scrape |
| Grafana | Docker image `grafana/grafana` | 3000 | HTTP |
| Jaeger | Docker image `jaegertracing/all-in-one` | 16686/4318 | HTTP/OTLP |

---

## Nivel 3 — Diagrama de Componentes (Component Diagram)

### riesgo-service

**Pregunta:** ¿Cuáles son los componentes internos de riesgo-service?

```mermaid
flowchart TB
    classDef controller fill:#85bbf0,color:#000,stroke:#5d82a8
    classDef service fill:#facc15,color:#000,stroke:#ca9a0d
    classDef client fill:#f97316,color:#fff,stroke:#c05621
    classDef security fill:#ef4444,color:#fff,stroke:#b91c1c
    classDef pattern fill:#a78bfa,color:#fff,stroke:#7c3aed
    classDef config fill:#6b7280,color:#fff,stroke:#374151

    EXT_CLIENT["🌐 Cliente externo"]

    subgraph RS_INT["riesgo-service — Componentes internos"]
        AC["AuthController\n[Controller]\nExpone POST /api/auth/login\nDelega generación de JWT"]:::controller
        EC["EstudianteController\n[Controller]\nExpone POST /evaluar\nExpone GET /historial/{nombre}"]:::controller

        JF["JwtFilter + SecurityConfig\n[Security]\nValida Bearer token\nen cada request protegido"]:::security
        JU["JwtUtil\n[Security]\nGenera y valida tokens JWT\nHS256 + UTF-8 explícito"]:::security

        ERS["EvaluadorRiesgoService\n[Service]\nOrquesta la lógica de evaluación\nMantiene contadores Micrometer"]:::service

        AF["ApoyoFactory\n[Factory Pattern]\nCrea ApoyoAcademico o\nApoyoPsicologico según NivelRiesgo"]:::pattern
        OBS["NotificadorRiesgo\n[Observer Pattern]\nNotifica localmente ante\ncambio de nivel de riesgo"]:::pattern

        NC["NotificacionClient\n[HTTP Client]\n@CircuitBreaker @Retry @TimeLimiter\nLlama al notificacion-service\ncon UriComponentsBuilder (safe URL)"]:::client
        RC["ResilienceConfig\n[Config]\nRestTemplate con timeouts\nconnect:2s / read:5s"]:::config
    end

    NS_EXT["notificacion-service\n(contenedor externo)"]

    EXT_CLIENT -->|"HTTP request"| JF
    JF -->|"request autenticado"| AC & EC
    AC --> JU
    EC --> ERS
    ERS --> AF
    ERS --> OBS
    ERS --> NC
    NC --> RC
    NC -->|"REST HTTP"| NS_EXT
```

### notificacion-service

**Pregunta:** ¿Cuáles son los componentes internos de notificacion-service?

```mermaid
flowchart TB
    classDef controller fill:#85bbf0,color:#000,stroke:#5d82a8
    classDef service fill:#facc15,color:#000,stroke:#ca9a0d
    classDef repo fill:#34d399,color:#000,stroke:#059669
    classDef model fill:#94a3b8,color:#000,stroke:#475569
    classDef db fill:#23652c,color:#fff,stroke:#164d20

    RS_EXT["riesgo-service\n(cliente HTTP interno)"]

    subgraph NS_INT["notificacion-service — Componentes internos"]
        NC2["NotificacionController\n[Controller]\nExpone POST /api/notificaciones/enviar\nExpone GET /historial/{nombre}\nExpone GET /health"]:::controller

        NS2["NotificacionService\n[Service]\nProcesa y persiste notificaciones\nConsulta historial ordenado por fecha\nIncluye campo creadoEn en respuesta"]:::service

        NR["NotificacionRepository\n[Repository]\nJPA — Spring Data\nfindByNombreIgnoreCaseOrderByCreadoEnDesc"]:::repo

        NE["NotificacionEntity\n[Model/Entity]\nid, nombre, nivel, creadoEn\n@Entity @Table(notificaciones)"]:::model
        NReq["NotificacionRequest\n[DTO]\n@NotBlank nombre\n@Pattern nivel (VERDE|AMARILLO|ROJO)"]:::model
    end

    H2[("H2 File DB\n./data/notificaciones")]:::db

    RS_EXT -->|"POST /enviar\nGET /historial"| NC2
    NC2 --> NS2
    NS2 --> NR
    NR --> NE
    NE --> H2
    NC2 -.->|"usa DTO"| NReq
```

---

## Nivel 4 — Código (Code Level)

El nivel 4 es opcional en C4 y generalmente se omite para evitar documentación que se desincroniza con el código. En este proyecto, el código fuente es la fuente de verdad:

| Clase | Archivo |
|-------|---------|
| `EvaluadorRiesgoService` | `riesgo-service/src/main/java/com/sabana/riesgo/service/EvaluadorRiesgoService.java` |
| `NotificacionClient` | `riesgo-service/src/main/java/com/sabana/riesgo/client/NotificacionClient.java` |
| `ApoyoFactory` | `riesgo-service/src/main/java/com/sabana/riesgo/factory/ApoyoFactory.java` |
| `JwtUtil` | `riesgo-service/src/main/java/com/sabana/riesgo/security/JwtUtil.java` |
| `NotificacionService` | `notificacion-service/src/main/java/com/sabana/notificacion/service/NotificacionService.java` |
| `NotificacionEntity` | `notificacion-service/src/main/java/com/sabana/notificacion/model/NotificacionEntity.java` |

---

## Comparación: C4 vs 4+1

| Nivel C4 | Vista 4+1 equivalente | Enfoque |
|----------|-----------------------|---------|
| Contexto (L1) | Escenarios (+1) | Sistema en su entorno |
| Contenedores (L2) | Física + Desarrollo | Servicios desplegables |
| Componentes (L3) | Lógica + Desarrollo | Clases e interfaces |
| Código (L4) | Lógica (detallada) | Implementación concreta |
