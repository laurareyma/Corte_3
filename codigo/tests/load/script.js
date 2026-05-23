import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  stages: [
    { duration: '30s', target: 50 }, // rampa de subida a 50 usuarios
    { duration: '1m', target: 50 },  // mantenerse en 50 usuarios
    { duration: '30s', target: 0 },  // rampa de bajada
  ],
};

export default function () {
  const res = http.get('http://localhost:8080/api/estudiantes/health');
  check(res, {
    'status was 200': (r) => r.status == 200,
  });
  sleep(1);
}
