# Modelo 4+1 — Botón de Pánico Académico (Corte 3)

**Autores:** Maria Alejandra Cabrera Arauz · Laura Vanessa Reyes Martinez · Juan Esteban Ramirez Hermosa  
**Curso:** Diseño y Arquitectura de Software  
**Profesor:** César Augusto Vega Fernández

---

El modelo 4+1 (Kruchten, 1995) describe la arquitectura del sistema desde cinco perspectivas
complementarias. Cada vista responde a las necesidades de un grupo de stakeholders diferente.

---

## Vista 1 — Lógica (Logical View)

**Audiencia:** Diseñadores y desarrolladores.  
**Describe:** Estructura de dominio del sistema: entidades, servicios y sus relaciones.

```mermaid
classDiagram
    class Estudiante {
        +String nombre
        +NivelRiesgo nivel
        +getNombre() String
        +getNivel() NivelRiesgo
    }

    class NivelRiesgo {
        <<enumeration>>
        VERDE
        AMARILLO
        ROJO
    }

    class EvaluadorRiesgoService {
        -List~RiesgoObserver~ observadores
        -NotificacionClient client
        -Counter estudiantesEvaluados
        -Counter alertasCriticas
        +evaluar(Estudiante) Map
        +consultarHistorial(String) Map
    }

    class ApoyoFactory {
        <<factory>>
        +crearApoyo(NivelRiesgo) Apoyo
    }

    class Apoyo {
        <<interface>>
        +ofrecer() String
    }

    class ApoyoAcademico {
        +ofrecer() String
    }

    class ApoyoPsicologico {
        +ofrecer() String
    }

    class RiesgoObserver {
        <<interface>>
        +actualizar(Estudiante)
    }

    class NotificadorRiesgo {
        +actualizar(Estudiante)
    }

    class NotificacionClient {
        -RestTemplate restTemplate
        +enviarNotificacion(Estudiante) CompletableFuture~String~
        +consultarHistorial(String) CompletableFuture~String~
        +fallbackNotificar(Estudiante, Throwable) CompletableFuture
        +fallbackHistorial(String, Throwable) CompletableFuture
    }

    class NotificacionEntity {
        +Long id
        +String nombre
        +String nivel
        +Instant creadoEn
    }

    class NotificacionService {
        +procesarNotificacion(NotificacionRequest) String
        +consultarHistorial(String) List~Map~
    }

    class JwtUtil {
        -String secret
        +generarToken(String) String
        +extraerUsuario(String) String
        +validarToken(String) boolean
    }

    Estudiante --> NivelRiesgo
    EvaluadorRiesgoService --> NotificacionClient
    EvaluadorRiesgoService --> ApoyoFactory
    EvaluadorRiesgoService --> RiesgoObserver
    ApoyoFactory ..> Apoyo
    ApoyoAcademico ..|> Apoyo
    ApoyoPsicologico ..|> Apoyo
    NotificadorRiesgo ..|> RiesgoObserver
    NotificacionService --> NotificacionEntity
```

**Patrones de diseño aplicados:**

| Patrón | Clase | Propósito |
|--------|-------|-----------|
| Factory | `ApoyoFactory` | Crea el tipo de apoyo correcto según nivel de riesgo |
| Observer | `RiesgoObserver` / `NotificadorRiesgo` | Notifica localmente ante cambio de estado |
| Circuit Breaker | `NotificacionClient` | Protege llamadas remotas al notificacion-service |

---

## Vista 2 — Desarrollo (Development / Implementation View)

**Audiencia:** Desarrolladores.  
**Describe:** Organización del código fuente en módulos y paquetes.

```mermaid
flowchart TB
    subgraph RS["📦 riesgo-service (com.sabana.riesgo)"]
        direction TB
        CTR["controller/\n AuthController\n EstudianteController"]
        SVC["service/\n EvaluadorRiesgoService"]
        CLI["client/\n NotificacionClient"]
        FAC["factory/\n ApoyoFactory\n ApoyoAcademico\n ApoyoPsicologico"]
        OBS["observer/\n RiesgoObserver\n NotificadorRiesgo"]
        MDL["model/\n Estudiante\n NivelRiesgo"]
        SEC["security/\n JwtUtil"]
        CFG["config/\n SecurityConfig\n ResilienceConfig"]

        CTR --> SVC
        SVC --> CLI
        SVC --> FAC
        SVC --> OBS
        CTR --> SEC
        CFG --> SEC
    end

    subgraph NS["📦 notificacion-service (com.sabana.notificacion)"]
        direction TB
        NCTRL["controller/\n NotificacionController"]
        NSVC["service/\n NotificacionService"]
        NREP["repository/\n NotificacionRepository"]
        NMDL["model/\n NotificacionEntity\n NotificacionRequest"]

        NCTRL --> NSVC
        NSVC --> NREP
        NREP --> NMDL
    end

    subgraph TST["🧪 Tests"]
        T1["riesgo-service/test\n EvaluadorRiesgoServiceTest\n NotificacionClientWireMockTest\n ApoyoFactoryTest\n JwtUtilTest\n RiesgoApplicationTest"]
        T2["notificacion-service/test\n NotificacionControllerTest\n NotificacionRepositoryTest\n NotificacionServiceTest\n IntegrationTest"]
    end
```

**Dependencias externas clave:**

| Librería | Versión | Uso |
|----------|---------|-----|
| Spring Boot | 3.2.3 | Framework base |
| Resilience4j | 2.2.0 | Circuit Breaker, Retry, TimeLimiter |
| JJWT | 0.11.5 | Generación y validación de tokens JWT |
| WireMock | 3.4.2 | Mocking de HTTP en pruebas |
| JaCoCo | 0.8.11 | Cobertura de código (umbral ≥ 80%) |
| Allure | 2.25.0 | Reportes visuales de pruebas |

---

## Vista 3 — Proceso (Process View)

**Audiencia:** Integradores, QA.  
**Describe:** Comportamiento dinámico del sistema: flujos de ejecución, concurrencia y resiliencia.

### Flujo principal: Evaluar riesgo

```mermaid
sequenceDiagram
    actor U as Usuario/Sistema
    participant AC as AuthController
    participant JF as JwtFilter
    participant EC as EstudianteController
    participant ERS as EvaluadorRiesgoService
    participant NC as NotificacionClient
    participant CB as CircuitBreaker
    participant NS as notificacion-service
    participant OBS as Prometheus/Jaeger

    U->>AC: POST /api/auth/login {"usuario":"admin"}
    AC-->>U: {"token": "<JWT>", "tipo": "Bearer"}

    U->>JF: POST /api/estudiantes/evaluar (Bearer JWT)
    JF->>JF: validarToken(JWT)
    JF-->>EC: request autenticado

    EC->>ERS: evaluar(Estudiante{nombre, nivel})
    ERS->>OBS: estudiantesEvaluadosCounter.increment()

    alt nivel == ROJO o AMARILLO
        ERS->>ERS: notificar observadores locales
        ERS->>ERS: ApoyoFactory.crearApoyo(nivel)
        ERS->>NC: enviarNotificacion(estudiante)

        NC->>CB: verificar estado del circuito
        alt Circuito CERRADO
            CB->>NS: POST /api/notificaciones/enviar
            NS-->>CB: 200 OK
            CB-->>NC: respuesta
        else Circuito ABIERTO o timeout
            CB-->>NC: activar fallback
            NC-->>ERS: "Notificación no disponible temporalmente"
        end

        ERS-->>EC: {estudiante, nivelRiesgo, apoyo, notificacion}
    else nivel == VERDE
        ERS-->>EC: {estudiante, "VERDE", "Sin riesgo crítico"}
    end

    EC-->>U: 200 OK + resultado
    EC->>OBS: traza distribuida (OTLP → Jaeger)
```

### Flujo de resiliencia: Circuit Breaker

```mermaid
stateDiagram-v2
    [*] --> CERRADO

    CERRADO --> ABIERTO: Tasa de fallos ≥ 50%\n(ventana de 5 llamadas)
    ABIERTO --> SEMI_ABIERTO: Espera 10s
    SEMI_ABIERTO --> CERRADO: 2 llamadas de prueba exitosas
    SEMI_ABIERTO --> ABIERTO: Llamada de prueba falla

    CERRADO: CERRADO\nLlamadas pasan normalmente
    ABIERTO: ABIERTO\nFallback inmediato (sin llamar al servicio)
    SEMI_ABIERTO: SEMI-ABIERTO\nPrueba de recuperación
```

---

## Vista 4 — Física (Physical / Deployment View)

**Audiencia:** DevOps, operaciones, infraestructura.  
**Describe:** Cómo se despliegan los componentes sobre la infraestructura real.

### Despliegue local (Docker Compose)

```mermaid
flowchart TB
    subgraph HOST["🖥️ Máquina del desarrollador"]
        subgraph APPS["Procesos Java (spring-boot:run)"]
            RS["riesgo-service\n:8080"]
            NS["notificacion-service\n:8081"]
        end

        subgraph DOCKER["Contenedores Docker (docker-compose up)"]
            PROM["Prometheus\n:9090"]
            GRAF["Grafana\n:3000"]
            JAE["Jaeger\n:16686 / :4318"]
        end

        RS -->|scrape /actuator/prometheus| PROM
        NS -->|scrape /actuator/prometheus| PROM
        PROM --> GRAF
        RS -->|OTLP traces| JAE
        NS -->|OTLP traces| JAE
    end

    CLIENT["🌐 Cliente\n(curl / Postman / Newman)"] -->|JWT + REST :8080| RS
    RS -->|REST :8081| NS
```

### Despliegue en producción (Kubernetes)

```mermaid
flowchart TB
    subgraph K8S["☸️ Cluster Kubernetes"]
        subgraph NS_K8S["Namespace: default"]
            subgraph RS_DEP["Deployment: riesgo-service (2 réplicas)"]
                RS1["Pod riesgo-service-1\n:8080"]
                RS2["Pod riesgo-service-2\n:8080"]
            end
            SVC_RS["Service: riesgo-service\nNodePort :30080"]

            subgraph NS_DEP["Deployment: notificacion-service (2 réplicas)"]
                NS1["Pod notificacion-service-1\n:8081"]
                NS2["Pod notificacion-service-2\n:8081"]
            end
            SVC_NS["Service: notificacion-service\n:8081"]

            subgraph SEC_K8S["Seguridad K8s"]
                RBAC["RBAC\n(Role + RoleBinding)"]
                NP["NetworkPolicy\nDefault-Deny\n+ Allow RS→NS"]
                SECRETS["Secrets\nJWT_SECRET"]
            end
        end
    end

    LB["🌐 Ingress / LoadBalancer"] --> SVC_RS
    SVC_RS --> RS1 & RS2
    RS1 & RS2 -->|NetworkPolicy permite| SVC_NS
    SVC_NS --> NS1 & NS2

    style SEC_K8S fill:#fff3cd
```

**Configuración de alta disponibilidad:**

| Aspecto | Configuración |
|---------|--------------|
| Réplicas por servicio | 2 pods |
| Liveness probe | `GET /api/.../health` cada 30s |
| Readiness probe | `GET /api/.../health` cada 10s |
| Política de red | Default-deny + allow explícito RS→NS:8081 |
| Control de acceso | RBAC con permisos mínimos (solo lectura de pods) |
| Secretos | `JWT_SECRET` via Kubernetes Secrets |

---

## Vista +1 — Escenarios (Use Cases)

**Audiencia:** Todos los stakeholders.  
**Describe:** Los casos de uso que guían y validan las cuatro vistas anteriores.

```mermaid
flowchart LR
    DOCENTE["👩‍🏫 Docente /\nSistema académico"]
    ADMIN["👨‍💼 Administrador"]

    subgraph SISTEMA["Sistema — Botón de Pánico Académico"]
        UC1["UC1: Autenticarse\n(obtener JWT)"]
        UC2["UC2: Evaluar riesgo\nde estudiante"]
        UC3["UC3: Consultar\nhistorial de alertas"]
        UC4["UC4: Monitorear\nsalud del sistema"]
        UC5["UC5: Ver métricas\ny trazas"]
    end

    DOCENTE --> UC1
    DOCENTE --> UC2
    DOCENTE --> UC3
    ADMIN --> UC4
    ADMIN --> UC5
```

| Caso de uso | Endpoints involucrados | Vista Lógica | Vista Proceso | Vista Física |
|-------------|----------------------|--------------|---------------|--------------|
| UC1: Autenticarse | `POST /api/auth/login` | `AuthController`, `JwtUtil` | Genera token firmado HS256 | riesgo-service pod |
| UC2: Evaluar riesgo | `POST /api/estudiantes/evaluar` | `EvaluadorRiesgoService`, `ApoyoFactory`, `NotificacionClient` | Circuit Breaker + Observer + métricas | riesgo-service → notificacion-service |
| UC3: Consultar historial | `GET /api/estudiantes/historial/{nombre}` | `NotificacionClient` → `NotificacionService` | Circuit Breaker + H2 query | Cross-service REST call |
| UC4: Health check | `GET .../health` | Ambos controllers | Respuesta inmediata, sin JWT | Kubernetes probes |
| UC5: Métricas/trazas | `/actuator/prometheus`, Grafana, Jaeger | Micrometer, OpenTelemetry | Exportación asíncrona OTLP | Stack de observabilidad |

---

## Resumen de correspondencia vistas

| Vista | Stakeholder principal | Herramienta/Artefacto |
|-------|-----------------------|----------------------|
| Lógica | Arquitecto, desarrollador | Diagrama de clases (Mermaid) |
| Desarrollo | Desarrollador, CI/CD | Estructura de paquetes Maven |
| Proceso | QA, integrador | Diagrama de secuencia + estados |
| Física | DevOps, infraestructura | docker-compose.yml, k8s/ manifests |
| Escenarios | Cliente, PO | Casos de uso + tabla de trazabilidad |
