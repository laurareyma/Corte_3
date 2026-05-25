import http from 'k6/http';
import { check, group, sleep } from 'k6';
import { Rate, Trend } from 'k6/metrics';

// Métricas personalizadas
const errorRate   = new Rate('errors');
const evalLatency = new Trend('evaluar_latency_ms');

export const options = {
  stages: [
    { duration: '30s', target: 10 },  // calentamiento
    { duration: '1m',  target: 50 },  // carga sostenida
    { duration: '30s', target: 100 }, // pico
    { duration: '30s', target: 0 },   // bajada
  ],
  thresholds: {
    http_req_failed:    ['rate<0.05'],       // menos del 5% de errores HTTP
    http_req_duration:  ['p(95)<2000'],      // p95 < 2 s
    evaluar_latency_ms: ['p(95)<1500'],      // p95 de /evaluar < 1.5 s
    errors:             ['rate<0.05'],
  },
};

const BASE_URL  = __ENV.BASE_URL  || 'http://localhost:8080';
const NOTIF_URL = __ENV.NOTIF_URL || 'http://localhost:8081';

const NIVELES   = ['ROJO', 'AMARILLO', 'VERDE'];
const NOMBRES   = ['Luis García', 'Ana López', 'Carlos Pérez', 'María Martínez', 'Juan Rodríguez'];

function randomItem(arr) {
  return arr[Math.floor(Math.random() * arr.length)];
}

// Obtiene un token JWT al inicio de cada VU
function obtenerToken() {
  const res = http.post(
    `${BASE_URL}/api/auth/login`,
    JSON.stringify({ usuario: 'admin' }),
    { headers: { 'Content-Type': 'application/json' } }
  );
  check(res, { 'login status 200': (r) => r.status === 200 });
  try {
    return res.json('token');
  } catch (_) {
    return '';
  }
}

export function setup() {
  // Verificar que ambos servicios estén disponibles antes de la prueba
  const riesgoHealth = http.get(`${BASE_URL}/api/estudiantes/health`);
  const notifHealth  = http.get(`${NOTIF_URL}/api/notificaciones/health`);

  check(riesgoHealth, { 'riesgo-service UP': (r) => r.status === 200 });
  check(notifHealth,  { 'notificacion-service UP': (r) => r.status === 200 });
}

export default function () {
  const token = obtenerToken();
  if (!token) {
    errorRate.add(1);
    return;
  }

  const headers = {
    'Content-Type':  'application/json',
    'Authorization': `Bearer ${token}`,
  };

  const nombre = randomItem(NOMBRES);
  const nivel  = randomItem(NIVELES);

  // -------------------------------------------------------------------------
  group('health checks', () => {
    const res = http.get(`${BASE_URL}/api/estudiantes/health`);
    const ok  = check(res, { 'health 200': (r) => r.status === 200 });
    errorRate.add(!ok);
  });

  sleep(0.5);

  // -------------------------------------------------------------------------
  group('evaluar riesgo', () => {
    const start = Date.now();
    const res = http.post(
      `${BASE_URL}/api/estudiantes/evaluar`,
      JSON.stringify({ nombre, nivel }),
      { headers }
    );
    evalLatency.add(Date.now() - start);

    const ok = check(res, {
      'evaluar status 200': (r) => r.status === 200,
      'respuesta tiene nivelRiesgo': (r) => {
        try { return r.json('nivelRiesgo') !== undefined; } catch (_) { return false; }
      },
    });
    errorRate.add(!ok);
  });

  sleep(1);

  // -------------------------------------------------------------------------
  group('consultar historial', () => {
    const res = http.get(
      `${BASE_URL}/api/estudiantes/historial/${encodeURIComponent(nombre)}`,
      { headers }
    );
    const ok = check(res, {
      'historial status 200': (r) => r.status === 200,
      'respuesta tiene historial': (r) => {
        try { return r.json('historial') !== undefined; } catch (_) { return false; }
      },
    });
    errorRate.add(!ok);
  });

  sleep(1);
}
