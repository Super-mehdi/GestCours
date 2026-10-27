// Tests de charge GestCours — scénarios décrits dans SCENARIOS.md
// Lancement : k6 run -e PROFILE=smoke|load|stress|spike load-tests/scenarios.js
import http from 'k6/http';
import { check, group, sleep } from 'k6';
import { SharedArray } from 'k6/data';

const BASE = __ENV.BASE_URL || 'https://gestcours-qfgb.onrender.com/api';
const PROFILE = __ENV.PROFILE || 'smoke';

const STUDENTS = new SharedArray('students', () => [
  'youssef.elamrani@example.com', 'sara.benali@example.com', 'adam.alaoui@example.com',
  'salma.idrissi@example.com', 'omar.bennani@example.com', 'nora.chakir@example.com',
  'mehdi.fassi@example.com', 'lina.tazi@example.com',
]);
const ADMIN = { email: 'admin@portal.com', password: 'admin123' };

// Paliers de VU au total ; chaque scénario en reçoit sa part (SCENARIOS.md)
const PROFILES = {
  smoke: [{ duration: '10s', target: 4 }, { duration: '2m', target: 4 }],
  load: [{ duration: '1m', target: 20 }, { duration: '5m', target: 20 }, { duration: '1m', target: 0 }],
  stress: [10, 25, 50, 75, 100, 150].flatMap((t) => [
    { duration: '30s', target: t }, { duration: '2m', target: t },
  ]).concat([{ duration: '1m', target: 0 }]),
  spike: [{ duration: '10s', target: 100 }, { duration: '1m', target: 100 }, { duration: '20s', target: 0 }],
};
const SHARES = { s1_etudiant_consulte: 0.6, s2_etudiant_inscription: 0.25, s3_admin_demandes: 0.1, s4_admin_catalogue: 0.05 };

function scenario(name) {
  return {
    executor: 'ramping-vus',
    exec: name,
    startVUs: 0,
    stages: PROFILES[PROFILE].map((s) => ({
      duration: s.duration,
      target: s.target === 0 ? 0 : Math.max(1, Math.round(s.target * SHARES[name])),
    })),
    gracefulRampDown: '30s',
  };
}

export const options = {
  scenarios: Object.fromEntries(Object.keys(SHARES).map((n) => [n, scenario(n)])),
  thresholds: {
    http_req_failed: ['rate<0.01'],
    http_req_duration: ['p(95)<1000'],
    checks: ['rate>0.99'],
  },
  summaryTrendStats: ['avg', 'med', 'p(90)', 'p(95)', 'max'],
};

// Pause « humaine » entre deux actions
const think = (min, max) => sleep(min + Math.random() * (max - min));

function ok(res, label) {
  return check(res, { [`${label} -> 200`]: (r) => r.status === 200 });
}

function login(email, password) {
  const res = http.post(`${BASE}/auth/login`, JSON.stringify({ email, password }), {
    headers: { 'Content-Type': 'application/json' },
    tags: { name: 'POST /auth/login' },
  });
  if (!ok(res, 'login')) return null;
  const body = res.json();
  return {
    studentId: body.studentId,
    params: (name) => ({
      headers: { Authorization: `Bearer ${body.token}`, 'Content-Type': 'application/json' },
      tags: { name },
    }),
  };
}

function studentLogin() {
  group('Page login', () => {
    ok(http.get(`${BASE}/auth/test-credentials`, { tags: { name: 'GET /auth/test-credentials' } }), 'test-credentials');
  });
  think(2, 5);
  const email = STUDENTS[Math.floor(Math.random() * STUDENTS.length)];
  return login(email, 'student123');
}

// Ce que charge student-portal.component à l'ouverture
function loadPortal(s) {
  let courses = [];
  let regs = [];
  group('Portail étudiant', () => {
    const c = http.get(`${BASE}/courses`, s.params('GET /courses'));
    const r = http.get(`${BASE}/registrations/student/${s.studentId}`, s.params('GET /registrations/student/{id}'));
    if (ok(c, 'courses')) courses = c.json();
    if (ok(r, 'student registrations')) regs = r.json();
  });
  return { courses, regs };
}

export function s1_etudiant_consulte() {
  const s = studentLogin();
  if (!s) return;
  loadPortal(s);
  think(5, 15);
  loadPortal(s);
  think(5, 10);
}

export function s2_etudiant_inscription() {
  const s = studentLogin();
  if (!s) return;
  const { courses, regs } = loadPortal(s);
  think(5, 15);

  const taken = new Set(regs.map((r) => r.courseId));
  // Les cours LT-* sont temporaires (S4 les supprime) : un étudiant réel ne les verrait pas
  const free = courses.filter((c) => !taken.has(c.id) && !c.code.startsWith('LT-'));
  if (free.length === 0) return; // déjà inscrit partout : simple consultation

  const course = free[Math.floor(Math.random() * free.length)];
  group("Demande d'inscription", () => {
    const res = http.post(
      `${BASE}/registrations/request?studentId=${s.studentId}&courseId=${course.id}`, null,
      Object.assign(s.params('POST /registrations/request'), {
        // Deux VU du même étudiant peuvent demander le même cours : refus métier, pas une panne
        responseCallback: http.expectedStatuses(200, 400, 409),
      }),
    );
    check(res, { 'request accepted or refused cleanly': (r) => [200, 400, 409].includes(r.status) });
  });
  think(2, 4);
  loadPortal(s);
  think(3, 6);
}

export function s3_admin_demandes() {
  const s = login(ADMIN.email, ADMIN.password);
  if (!s) return;
  let pending = [];
  group('Page Course Requests', () => {
    const res = http.get(`${BASE}/registrations/pending`, s.params('GET /registrations/pending'));
    if (ok(res, 'pending')) pending = res.json();
  });
  think(3, 6);

  if (pending.length > 0) {
    const id = pending[0].id;
    const action = Math.random() < 0.5 ? 'approve' : 'reject';
    group('Traiter une demande', () => {
      const res = http.put(`${BASE}/registrations/${id}/${action}`, null,
        Object.assign(s.params(`PUT /registrations/{id}/${action}`), {
          // Un autre admin peut avoir déjà traité la même demande
          responseCallback: http.expectedStatuses(200, 400, 404, 409),
        }));
      check(res, { 'decision handled': (r) => [200, 400, 404, 409].includes(r.status) });
      ok(http.get(`${BASE}/registrations/pending`, s.params('GET /registrations/pending')), 'pending reload');
    });
    think(2, 4);
  }

  group('Page Students & Enrollments', () => {
    ok(http.get(`${BASE}/students`, s.params('GET /students')), 'students');
    ok(http.get(`${BASE}/courses`, s.params('GET /courses')), 'courses');
    ok(http.get(`${BASE}/registrations`, s.params('GET /registrations')), 'registrations');
  });
  think(5, 10);
}

export function s4_admin_catalogue() {
  const s = login(ADMIN.email, ADMIN.password);
  if (!s) return;
  const list = () => ok(http.get(`${BASE}/courses`, s.params('GET /courses')), 'courses');

  group('Page Courses Catalog', list);
  think(3, 6);

  let course = null;
  group('Ajouter un cours', () => {
    const body = {
      code: `LT-${__VU}-${__ITER}`,
      name: 'Cours de test de charge',
      description: 'Créé par k6, supprimé en fin de parcours',
      capacity: 30,
    };
    const res = http.post(`${BASE}/courses`, JSON.stringify(body), s.params('POST /courses'));
    if (ok(res, 'create course')) course = res.json();
    list();
  });
  if (!course) return;
  think(3, 6);

  group('Modifier le cours', () => {
    course.capacity = 40;
    ok(http.put(`${BASE}/courses/${course.id}`, JSON.stringify(course), s.params('PUT /courses/{id}')), 'update course');
    list();
  });
  think(2, 4);

  group('Supprimer le cours', () => {
    ok(http.del(`${BASE}/courses/${course.id}`, null, s.params('DELETE /courses/{id}')), 'delete course');
    list();
  });
  think(3, 6);
}
