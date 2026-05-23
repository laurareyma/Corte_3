# Guía Paso a Paso: Ejecutar el Proyecto en Visual Studio Code

Este proyecto está diseñado con Spring Boot (Java), Docker y ahora incluye herramientas DevSecOps (Fase 3).

## Requisitos Previos

1. **Visual Studio Code** instalado.
2. Extensiones de VS Code:
   - **Extension Pack for Java** (de Microsoft).
   - **Spring Boot Extension Pack** (de VMware).
   - **Docker** (de Microsoft).
3. **Java Development Kit (JDK) 17** instalado en tu sistema.
4. **Maven** instalado y en el PATH (opcional, el IDE tiene su propio wrapper, pero es útil).
5. **Docker Desktop** instalado y en ejecución.

---

## Paso a Paso

### 1. Abrir el proyecto
1. Abre Visual Studio Code.
2. Ve a `File > Open Folder...` y selecciona la carpeta descomprimida `DAO-Corte2`.
3. Espera unos segundos a que la extensión de Java reconozca los subproyectos (verás un icono de pulgar hacia arriba abajo a la derecha o dirá "Java Projects loaded").

### 2. Iniciar Infraestructura Base (Docker)
Este proyecto requiere bases de datos (si aplica) y herramientas de observabilidad (Prometheus, Grafana).
1. Abre una terminal integrada en VS Code (`Terminal > New Terminal`).
2. Ejecuta el comando para levantar los contenedores en segundo plano:
   ```bash
   docker-compose up -d
   ```
3. Verifica que los contenedores estén corriendo en Docker Desktop.

### 3. Ejecutar los Microservicios
Debemos iniciar los dos servicios de forma independiente.

**A. Iniciar Notificacion-Service:**
1. En la pestaña **"Spring Boot Dashboard"** (en la barra lateral izquierda de VS Code), busca `notificacion-service`.
2. Haz clic derecho y selecciona **"Start"** (o presiona el botón de *Play* ▶️).
3. Asegúrate de que inicie en el puerto `8081`.

**B. Iniciar Riesgo-Service:**
1. En el mismo **"Spring Boot Dashboard"**, busca `riesgo-service`.
2. Haz clic en **"Start"** ▶️.
3. Asegúrate de que inicie en el puerto `8080`.

> **Alternativa por Terminal:**
> Puedes abrir dos terminales en VS Code y ejecutar:
> Terminal 1: `cd notificacion-service && mvn spring-boot:run`
> Terminal 2: `cd riesgo-service && mvn spring-boot:run`

### 4. Ejecutar las Pruebas

#### Pruebas Unitarias y de Integración:
Ve a la sección **"Testing"** de VS Code (el icono de probeta en la barra izquierda) y ejecuta todas las pruebas. Verás cómo corren las pruebas de Mockito y WireMock.

#### Pruebas de API (Postman):
1. Abre **Postman**.
2. Ve a `File > Import`.
3. Selecciona el archivo `tests/api/postman_collection.json` que viene en la carpeta del proyecto.
4. Ejecuta la colección contra tus servicios corriendo localmente.

#### Pruebas de Carga (k6):
1. Si tienes [k6 instalado](https://k6.io/docs/get-started/installation/), abre una terminal en VS Code.
2. Ejecuta:
   ```bash
   k6 run tests/load/script.js
   ```

### 5. Revisar el Pipeline DevSecOps
Si subes este código a un repositorio en GitHub, las Actions (en `.github/workflows/ci-cd-pipeline.yml`) se ejecutarán automáticamente al hacer `push`. Validarán calidad (SonarQube), seguridad de dependencias, escaneo de contenedores (Trivy) y secretos.
