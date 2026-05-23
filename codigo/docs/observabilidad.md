# Observabilidad del Sistema

## Herramientas Implementadas

### 1. Prometheus — Recolección de Métricas

Prometheus hace scraping cada 15 segundos a los endpoints `/actuator/prometheus`
de ambos servicios.

**Métricas personalizadas:**
- `estudiantes_evaluados_total` — Contador de estudiantes evaluados
- `alertas_criticas_total` — Contador de alertas nivel ROJO

**Métricas automáticas de Spring Boot:**
- `http_server_requests_seconds` — Latencia de endpoints REST
- `jvm_memory_used_bytes` — Uso de memoria JVM
- `resilience4j_circuitbreaker_state` — Estado del Circuit Breaker

**Acceso:** http://localhost:9090

### 2. Grafana — Visualización

Grafana se conecta a Prometheus como fuente de datos y permite crear dashboards.

**Configuración:**
1. Abrir http://localhost:3000 (admin / admin123)
2. Ir a Connections → Data Sources → Add → Prometheus
3. URL: `http://prometheus:9090` → Save & Test
4. Crear nuevo dashboard → Add Panel
5. Usar queries como:
   - `estudiantes_evaluados_total`
   - `alertas_criticas_total`
   - `rate(http_server_requests_seconds_count[1m])`

### 3. Jaeger — Tracing Distribuido

Jaeger captura las trazas de las peticiones que atraviesan los servicios,
permitiendo ver el flujo completo de una solicitud.

**Acceso:** http://localhost:16686

**Configuración en riesgo-service:**
```properties
management.tracing.sampling.probability=1.0
otel.exporter.otlp.endpoint=http://localhost:4318
otel.service.name=riesgo-service
```

## Endpoints de observabilidad expuestos

| Servicio | Endpoint | Descripción |
|---|---|---|
| riesgo-service | /actuator/health | Estado del servicio |
| riesgo-service | /actuator/prometheus | Métricas para Prometheus |
| riesgo-service | /actuator/metrics | Métricas en formato JSON |
| notificacion-service | /actuator/health | Estado del servicio |
| notificacion-service | /actuator/prometheus | Métricas para Prometheus |
