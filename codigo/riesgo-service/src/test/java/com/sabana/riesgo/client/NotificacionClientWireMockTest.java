package com.sabana.riesgo.client;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.sabana.riesgo.model.Estudiante;
import com.sabana.riesgo.model.NivelRiesgo;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.util.concurrent.ExecutionException;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Pruebas autónomas: WireMock simula el notificacion-service para verificar
 * el comportamiento del cliente HTTP ante respuestas exitosas y fallidas.
 */
@SpringBootTest
class NotificacionClientWireMockTest {

    static WireMockServer wireMock = new WireMockServer(
            WireMockConfiguration.wireMockConfig().dynamicPort()
    );

    @DynamicPropertySource
    static void overrideServiceUrl(DynamicPropertyRegistry registry) {
        wireMock.start();
        registry.add("notificacion.service.url", wireMock::baseUrl);
    }

    @Autowired
    NotificacionClient notificacionClient;

    @BeforeEach
    void resetMappings() {
        wireMock.resetAll();
    }

    @AfterEach
    void verifyNoUnmatchedRequests() {
        wireMock.findAllUnmatchedRequests().forEach(r ->
                System.err.println("WireMock — petición no mapeada: " + r));
    }

    // -----------------------------------------------------------------------
    // enviarNotificacion — respuesta exitosa
    // -----------------------------------------------------------------------

    @Test
    void enviarNotificacion_cuandoServicioResponde200_retornaRespuesta() throws Exception {
        wireMock.stubFor(post(urlEqualTo("/api/notificaciones/enviar"))
                .withHeader("Content-Type", containing("application/json"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", MediaType.TEXT_PLAIN_VALUE)
                        .withBody("OK notificado")));

        Estudiante estudiante = new Estudiante("Carlos", NivelRiesgo.ROJO);
        String resultado = notificacionClient.enviarNotificacion(estudiante).get();

        assertThat(resultado).isEqualTo("OK notificado");
        wireMock.verify(1, postRequestedFor(urlEqualTo("/api/notificaciones/enviar")));
    }

    // -----------------------------------------------------------------------
    // enviarNotificacion — fallback al caer el servicio
    // -----------------------------------------------------------------------

    @Test
    void enviarNotificacion_cuandoServicioCae_activaFallback() throws Exception {
        wireMock.stubFor(post(urlEqualTo("/api/notificaciones/enviar"))
                .willReturn(aResponse()
                        .withStatus(500)
                        .withFixedDelay(100)));

        Estudiante estudiante = new Estudiante("Ana", NivelRiesgo.AMARILLO);
        String resultado = notificacionClient.enviarNotificacion(estudiante).get();

        assertThat(resultado).contains("Notificación no disponible temporalmente");
    }

    // -----------------------------------------------------------------------
    // consultarHistorial — respuesta exitosa con array JSON
    // -----------------------------------------------------------------------

    @Test
    void consultarHistorial_cuandoServicioResponde200_retornaJson() throws Exception {
        String jsonBody = "[{\"nombre\":\"Carlos\",\"nivel\":\"ROJO\",\"creadoEn\":\"2024-01-01T00:00:00Z\"}]";

        wireMock.stubFor(get(urlEqualTo("/api/notificaciones/historial/Carlos"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBody(jsonBody)));

        String resultado = notificacionClient.consultarHistorial("Carlos").get();

        assertThat(resultado).isEqualTo(jsonBody);
        wireMock.verify(1, getRequestedFor(urlEqualTo("/api/notificaciones/historial/Carlos")));
    }

    // -----------------------------------------------------------------------
    // consultarHistorial — fallback al caer el servicio
    // -----------------------------------------------------------------------

    @Test
    void consultarHistorial_cuandoServicioCae_retornaArrayVacio() throws Exception {
        wireMock.stubFor(get(urlPathMatching("/api/notificaciones/historial/.*"))
                .willReturn(aResponse()
                        .withStatus(503)
                        .withFixedDelay(100)));

        String resultado = notificacionClient.consultarHistorial("Inexistente").get();

        assertThat(resultado).isEqualTo("[]");
    }

    // -----------------------------------------------------------------------
    // consultarHistorial — encoding de nombre con caracteres especiales
    // -----------------------------------------------------------------------

    @Test
    void consultarHistorial_nombreConEspacios_codificaUrlCorrectamente() throws Exception {
        wireMock.stubFor(get(urlEqualTo("/api/notificaciones/historial/Juan%20P%C3%A9rez"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBody("[]")));

        String resultado = notificacionClient.consultarHistorial("Juan Pérez").get();

        assertThat(resultado).isEqualTo("[]");
        wireMock.verify(1, getRequestedFor(urlEqualTo("/api/notificaciones/historial/Juan%20P%C3%A9rez")));
    }
}
