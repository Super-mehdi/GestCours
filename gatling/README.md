# Tests de charge Gatling

Mêmes 4 scénarios, pauses, répartition (60 / 25 / 10 / 5 %) et paliers que les tests k6 de [`../load-tests`](../load-tests/SCENARIOS.md),
pour comparer les deux outils. Simulation : [GestCoursSimulation.java](src/test/java/gestcours/GestCoursSimulation.java).

Prérequis : Java 17+ et Maven (Gatling est téléchargé par Maven, rien d'autre à installer).

## Avant chaque test
Réveiller le backend (~60 s de démarrage sur le plan gratuit Render) :
```bash
curl.exe https://gestcours-qfgb.onrender.com/api/auth/test-credentials
```

## Lancer (depuis ce dossier `gatling/`)
```bash
mvn gatling:test -Dprofile=smoke
```
Profils : `smoke` (4 utilisateurs, 2 min), `load` (20, ~7 min), `stress` (10 → 150 par paliers, ~16 min), `spike` (100 d'un coup, ~1 min 30).

Autre backend : ajouter `-DbaseUrl=http://localhost:8080/api`.

## Résultat
- Terminal : statistiques globales (OK / KO, percentiles) et verdict des assertions (p95 < 1 s, erreurs < 1 %).
  « BUILD FAILURE … assertions failed » signifie seulement qu'un seuil est dépassé : le rapport est quand même généré.
- Rapport HTML : `target/gatling/gestcourssimulation-<date>/index.html` (chemin affiché à la fin du test) :
  courbe des utilisateurs actifs, temps de réponse et erreurs dans le temps, détail par requête et par page (groupes).
