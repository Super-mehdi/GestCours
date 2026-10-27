# Tests de charge k6

Scénarios et répartition : voir [SCENARIOS.md](SCENARIOS.md). Script : [scenarios.js](scenarios.js).

## Installer k6 (Windows)
```bash
winget install k6 --source winget
```

## Avant chaque test
Réveiller le backend (plan gratuit Render : ~60 s de démarrage), sinon la première minute est faussée :
```bash
curl https://gestcours-qfgb.onrender.com/api/auth/test-credentials
```

## Lancer (depuis la racine du dépôt)
```bash
k6 run -e PROFILE=smoke load-tests/scenarios.js
```
Profils : `smoke` (4 VU, 2 min), `load` (20 VU, ~7 min), `stress` (10 → 150 VU par paliers, ~16 min), `spike` (100 VU d'un coup, ~1 min 30).

Avec rapport HTML (courbes VU / temps de réponse / erreurs) :
```bash
k6 run -e PROFILE=stress --out web-dashboard=export=load-tests/rapport-stress.html load-tests/scenarios.js
```

Autre backend (ex. local) :
```bash
k6 run -e PROFILE=smoke -e BASE_URL=http://localhost:8080/api load-tests/scenarios.js
```

## Lire le résultat
Le site « tient » tant que `http_req_failed < 1 %` et `http_req_duration p(95) < 1 s` (seuils affichés ✓/✗ en fin de test).
Avec le profil `stress`, la capacité = **le dernier palier de VU** où les deux seuils sont encore respectés
(à lire sur les courbes du rapport HTML : moment où le p95 ou les erreurs décollent → nombre de VU à cet instant).

Ne pas dépasser le profil `stress` sur l'instance Render partagée.
