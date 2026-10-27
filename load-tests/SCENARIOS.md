# Tests de charge k6 — scénarios proposés (à valider)

Cible : backend `https://gestcours-qfgb.onrender.com` (le frontend est un site statique : la charge porte sur l'API).
Chaque scénario reproduit exactement les appels que fait le frontend Angular, page par page, avec des pauses humaines (think time).

## Critères « le site tient »
- erreurs (`http_req_failed`) < 1 %
- 95 % des requêtes < 1 s (`p(95) < 1000 ms`)

Capacité = dernier palier de VU qui respecte les deux critères.

## Répartition des utilisateurs

| # | Scénario | Part | Comptes |
|---|---|---|---|
| S1 | Étudiant qui consulte | 60 % | 8 étudiants seedés (`student123`), tirés au hasard |
| S2 | Étudiant qui s'inscrit | 25 % | idem |
| S3 | Admin qui traite les demandes | 10 % | `admin@portal.com` |
| S4 | Admin qui gère le catalogue | 5 % | `admin@portal.com` |

## S1 — Étudiant qui consulte (60 %)
1. Ouvre la page de login → `GET /api/auth/test-credentials` — pause 2-5 s
2. Se connecte → `POST /api/auth/login`
3. Arrive sur son portail → `GET /api/courses` + `GET /api/registrations/student/{id}` — lit 5-15 s
4. Rafraîchit son portail plus tard → mêmes 2 appels — pause 5-10 s
5. Quitte

## S2 — Étudiant qui s'inscrit (25 %)
1-3. Comme S1
4. Choisit un cours où il n'est pas inscrit → `POST /api/registrations/request?studentId=&courseId=` — pause 2-4 s
5. Portail rechargé → `GET /api/courses` + `GET /api/registrations/student/{id}`
   (si déjà inscrit partout : simple consultation, le 4xx éventuel n'est pas compté comme panne)

## S3 — Admin qui traite les demandes (10 %)
1. Login → `POST /api/auth/login`
2. Page « Course Requests » → `GET /api/registrations/pending` — lit 3-6 s
3. Approuve ou rejette (50/50) la plus ancienne demande → `PUT /api/registrations/{id}/approve|reject`
4. Liste rechargée → `GET /api/registrations/pending`
5. Page « Students & Enrollments » → `GET /api/students` + `GET /api/courses` + `GET /api/registrations` — pause 5-10 s

## S4 — Admin qui gère le catalogue (5 %)
1. Login
2. Page « Courses Catalog » → `GET /api/courses` — pause 3-6 s
3. Ajoute un cours (code unique `LT-<vu>-<iter>`) → `POST /api/courses` → `GET /api/courses`
4. Le modifie (capacité) → `PUT /api/courses/{id}` → `GET /api/courses`
5. Le supprime (nettoyage) → `DELETE /api/courses/{id}` → `GET /api/courses`

## Profils de test (même script, profil choisi par `-e PROFILE=`)

| Profil | Montée | Durée |
|---|---|---|
| `smoke` | 2 VU | 1 min |
| `load` | 0 → 20 VU, palier 5 min | ~8 min |
| `stress` | paliers 10 → 25 → 50 → 75 → 100 → 150, 2 min chacun | ~14 min |
| `spike` | 0 → 100 VU en 10 s, 1 min, retombée | ~2 min |

## Points d'attention
- Le login (BCrypt) est l'appel le plus coûteux : une connexion par parcours, comme un vrai utilisateur.
- Réveiller le backend avant chaque test (cold start ≈ 60 s sur le plan gratuit).
- Base H2 en mémoire : les inscriptions créées par S2 s'accumulent jusqu'au prochain redémarrage.
