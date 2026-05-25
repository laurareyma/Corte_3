# Botón de Pánico Académico — Corte 3

**Diseño y Arquitectura de Software**  
**Profesor:** César Augusto Vega Fernández  
**Autores:** Maria Alejandra Cabrera Arauz · Laura Vanesa Reyes Martinez · Juan Esteban Ramirez Hermosa

---

## Descripción del sistema

Sistema para la Universidad de La Sabana que detecta estudiantes en riesgo académico y activa
el apoyo adecuado (académico o psicológico) mediante un "Botón de Pánico Académico".

En este tercer corte el sistema evoluciona de la arquitectura orientada a servicios (Corte 2) a
un sistema con pruebas automatizadas completas, pipeline CI/CD con DevSecOps y modelos
arquitectónicos 4+1 y C4 documentados.

---

## Arquitectura

```
Cliente (curl / Postman / Newman)
        │  JWT en Authorization header
        ▼
┌─────────────────────────────────────────┐
│          riesgo-service  :8080          │
│  ┌─────────────────────────────────┐   │
│  │ JWT Filter · AuthController     │   │
│  │ EstudianteController            │   │
│  │ EvaluadorRiesgoService          │   │
│  │  └─ ApoyoFactory (Factory)      │   │
│  │  └─ NotificadorRiesgo (Observer)│   │
│  │ NotificacionClient              │   │
│  │  └─ Circuit Breaker + Retry     │   │
│  └─────────────────────────────────┘   │
└──────────────┬──────────────────────────┘
               │  REST HTTP (Circuit Breaker)
               ▼
┌─────────────────────────────────────────┐
│       notificacion-service  :8081       │
│  ┌─────────────────────────────────┐   │
│  │ NotificacionController          │   │
│  │ NotificacionService             │   │
│  │ NotificacionRepository → H2 DB  │   │
│  └─────────────────────────────────┘   │
└─────────────────────────────────────────┘

Stack de observabilidad (Docker):
  Prometheus :9090 · Grafana :3000 · Jaeger :16686
```

---

## Instrucciones para ejecutar

### Requisitos

- Java 17+
- Maven 3.8+
- Docker + Docker Compose

### 1. Levantar infraestructura de monitoreo

```bash
docker-compose up -d
```

### 2. Iniciar notificacion-service

```bash
cd notificacion-service
mvn spring-boot:run
```

### 3. Iniciar riesgo-service (nueva terminal)

```bash
cd riesgo-service
JWT_SECRET=clave-super-secreta-sabana-2024-debe-ser-larga-minimo-32-chars \
mvn spring-boot:run
```

---

## Uso de la API

### Obtener token JWT

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"usuario": "admin"}'
```

### Evaluar riesgo de un estudiante

```bash
curl -X POST http://localhost:8080/api/estudiantes/evaluar \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{"nombre": "Maria García", "nivel": "ROJO"}'
```

### Consultar historial

```bash
curl -X GET "http://localhost:8080/api/estudiantes/historial/Maria%20Garc%C3%ADa" \
  -H "Authorization: Bearer <TOKEN>"
```

### Health checks (públicos)

```bash
curl http://localhost:8080/api/estudiantes/health
curl http://localhost:8081/api/notificaciones/health
```

---

## Ejecutar pruebas

```bash
# Pruebas unitarias + WireMock + integración + cobertura JaCoCo ≥80%
cd notificacion-service && mvn clean verify
cd ../riesgo-service && mvn clean verify

# Pruebas de API con Newman (requiere servicios corriendo)
newman run tests/api/postman_collection.json \
  --env-var BASE_URL=http://localhost:8080 \
  --env-var NOTIF_URL=http://localhost:8081

# Pruebas de carga con k6 (requiere servicios corriendo)
k6 run tests/load/script.js
```

---

## Pipeline CI/CD (GitHub Actions)

El pipeline se ejecuta automáticamente en cada push a `main`:

| Job | Herramientas | Resultado |
|-----|-------------|-----------|
| build-and-test | Maven, JUnit 5, WireMock, JaCoCo | Tests + cobertura ≥80% |
| sonarcloud | SonarCloud, sonar-maven-plugin | Quality Gate |
| security-sast | Semgrep, Gitleaks, OWASP Dep-Check | Reporte de vulnerabilidades |
| dast-api-tests | OWASP ZAP, Newman | Escaneo activo + API tests |
| docker-security | Docker, Trivy | Escaneo de imágenes |

> Configurar el secret `SONAR_TOKEN` en GitHub → Settings → Secrets para activar el análisis de SonarCloud.

---

## Observabilidad

| Herramienta | URL | Credenciales |
|------------|-----|-------------|
| Prometheus | http://localhost:9090 | — |
| Grafana | http://localhost:3000 | admin / admin123 |
| Jaeger | http://localhost:16686 | — |

**Métricas personalizadas:**
- `estudiantes_evaluados_total` — contador de evaluaciones realizadas
- `alertas_criticas_total` — contador de alertas nivel ROJO

---

## Patrones de resiliencia

| Patrón | Configuración |
|--------|--------------|
| Circuit Breaker | Se abre al 50% de fallos, ventana de 5 llamadas, espera 10s |
| Retry | 3 intentos con espera de 2s entre cada uno |
| TimeLimiter | Timeout de 5s por llamada asíncrona |
| Timeouts HTTP | connect: 2s, read: 5s en RestTemplate |
| Fallback | Respuesta local si notificacion-service no está disponible |

---

## Documentación

| Documento | Descripción |
|-----------|-------------|
| [docs/modelo-4mas1.md](docs/modelo-4mas1.md) | Modelo 4+1: vistas Lógica, Desarrollo, Proceso, Física y Escenarios |
| [docs/modelo-c4.md](docs/modelo-c4.md) | Modelo C4: Contexto, Contenedores y Componentes |
| [docs/pruebas-cicd.md](docs/pruebas-cicd.md) | Estrategia de pruebas, CI/CD, DevSecOps y retos técnicos |
| [docs/arquitectura.md](docs/arquitectura.md) | Evolución de la arquitectura (Corte 1 → 2 → 3) |
| [docs/seguridad.md](docs/seguridad.md) | Implementación JWT y decisiones de seguridad |
| [docs/resiliencia.md](docs/resiliencia.md) | Circuit Breaker, Retry, Fallback |
| [docs/observabilidad.md](docs/observabilidad.md) | Prometheus, Grafana, Jaeger |

---

## Estructura del repositorio

```
codigo/
├── riesgo-service/              # Microservicio de evaluación de riesgo
│   ├── src/main/java/...
│   ├── src/test/java/...
│   └── Dockerfile
├── notificacion-service/        # Microservicio de notificaciones
│   ├── src/main/java/...
│   ├── src/test/java/...
│   └── Dockerfile
├── tests/
│   ├── api/postman_collection.json   # Pruebas de API (Newman)
│   └── load/script.js               # Pruebas de carga (k6)
├── k8s/                         # Manifests de Kubernetes
│   ├── riesgo-deployment.yaml
│   ├── notificacion-deployment.yaml
│   └── security/rbac.yaml, network-policies.yaml
├── monitoring/prometheus.yml    # Configuración de Prometheus
├── docker-compose.yml           # Stack de observabilidad local
├── .github/workflows/
│   └── ci-cd-pipeline.yml       # Pipeline GitHub Actions
├── .zap/rules.tsv               # Reglas OWASP ZAP
└── docs/                        # Documentación completa
```
