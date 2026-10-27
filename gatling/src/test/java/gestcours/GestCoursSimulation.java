package gestcours;

import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

/**
 * Tests de charge GestCours : mêmes 4 scénarios, pauses et paliers que load-tests/scenarios.js (k6),
 * pour pouvoir comparer les deux outils.
 * Lancement : mvn gatling:test -Dprofile=smoke|load|stress|spike
 */
public class GestCoursSimulation extends Simulation {

  static final String BASE = System.getProperty("baseUrl", "https://gestcours-qfgb.onrender.com/api");
  static final String PROFILE = System.getProperty("profile", "smoke");

  HttpProtocolBuilder httpProtocol = http
      .baseUrl(BASE)
      .acceptHeader("application/json")
      .contentTypeHeader("application/json");

  FeederBuilder<String> etudiants = csv("etudiants.csv").random();

  static final String AUTH = "Bearer #{token}";

  // ---------- briques communes ----------

  ChainBuilder pageLogin = exec(http("GET /auth/test-credentials").get("/auth/test-credentials"));

  ChainBuilder loginEtudiant = feed(etudiants)
      .exec(http("POST /auth/login").post("/auth/login")
          .body(StringBody("{\"email\":\"#{email}\",\"password\":\"#{password}\"}"))
          .check(jsonPath("$.token").saveAs("token"))
          .check(jsonPath("$.studentId").saveAs("studentId")));

  ChainBuilder loginAdmin = exec(http("POST /auth/login").post("/auth/login")
      .body(StringBody("{\"email\":\"admin@portal.com\",\"password\":\"admin123\"}"))
      .check(jsonPath("$.token").saveAs("token")));

  // Ce que charge student-portal.component à l'ouverture
  ChainBuilder portail = group("Portail étudiant").on(
      exec(http("GET /courses").get("/courses").header("Authorization", AUTH)
          .check(jsonPath("$[*].id").findAll().optional().saveAs("courseIds"))
          .check(jsonPath("$[*].code").findAll().optional().saveAs("courseCodes")))
      .exec(http("GET /registrations/student/{id}").get("/registrations/student/#{studentId}")
          .header("Authorization", AUTH)
          .check(jsonPath("$[*].courseId").findAll().optional().saveAs("takenIds"))));

  ChainBuilder listeCours = exec(http("GET /courses").get("/courses").header("Authorization", AUTH));

  // ---------- S1 : étudiant qui consulte (60 %) ----------
  ScenarioBuilder s1 = scenario("S1 - Étudiant qui consulte")
      .exec(group("Page login").on(pageLogin))
      .pause(2, 5)
      .exec(loginEtudiant)
      .exec(portail)
      .pause(5, 15)
      .exec(portail)
      .pause(5, 10);

  // ---------- S2 : étudiant qui s'inscrit (25 %) ----------
  ScenarioBuilder s2 = scenario("S2 - Étudiant qui s'inscrit")
      .exec(group("Page login").on(pageLogin))
      .pause(2, 5)
      .exec(loginEtudiant)
      .exec(portail)
      .pause(5, 15)
      .exec(session -> {
        List<String> ids = session.contains("courseIds") ? session.getList("courseIds") : List.of();
        List<String> codes = session.contains("courseCodes") ? session.getList("courseCodes") : List.of();
        List<String> taken = session.contains("takenIds") ? session.getList("takenIds") : List.of();
        List<String> free = new ArrayList<>();
        for (int i = 0; i < ids.size(); i++) {
          // Les cours LT-* sont temporaires (S4 les supprime) : un étudiant réel ne les verrait pas
          if (!taken.contains(ids.get(i)) && !codes.get(i).startsWith("LT-")) free.add(ids.get(i));
        }
        return free.isEmpty() ? session.remove("courseId")
            : session.set("courseId", free.get(ThreadLocalRandom.current().nextInt(free.size())));
      })
      .doIf(session -> session.contains("courseId")).then(
          group("Demande d'inscription").on(
              exec(http("POST /registrations/request")
                  .post("/registrations/request?studentId=#{studentId}&courseId=#{courseId}")
                  .header("Authorization", AUTH)
                  // Deux utilisateurs du même étudiant peuvent demander le même cours : refus métier, pas une panne
                  .check(status().in(200, 400, 409))))
          .pause(2, 4)
          .exec(portail)
          .pause(3, 6));

  // ---------- S3 : admin qui traite les demandes (10 %) ----------
  ScenarioBuilder s3 = scenario("S3 - Admin qui traite les demandes")
      .exec(loginAdmin)
      .exec(group("Page Course Requests").on(
          exec(http("GET /registrations/pending").get("/registrations/pending").header("Authorization", AUTH)
              .check(jsonPath("$[0].id").optional().saveAs("pendingId")))))
      .pause(3, 6)
      .doIf(session -> session.contains("pendingId")).then(
          exec(session -> session.set("action", ThreadLocalRandom.current().nextBoolean() ? "approve" : "reject"))
          .exec(group("Traiter une demande").on(
              exec(http("PUT /registrations/{id}/#{action}").put("/registrations/#{pendingId}/#{action}")
                  .header("Authorization", AUTH)
                  // Un autre admin peut avoir déjà traité la même demande
                  .check(status().in(200, 400, 404, 409)))
              .exec(http("GET /registrations/pending").get("/registrations/pending").header("Authorization", AUTH))))
          .pause(2, 4))
      .exec(group("Page Students & Enrollments").on(
          exec(http("GET /students").get("/students").header("Authorization", AUTH))
          .exec(listeCours)
          .exec(http("GET /registrations").get("/registrations").header("Authorization", AUTH))))
      .pause(5, 10);

  // ---------- S4 : admin qui gère le catalogue (5 %) ----------
  ScenarioBuilder s4 = scenario("S4 - Admin qui gère le catalogue")
      .exec(loginAdmin)
      .exec(group("Page Courses Catalog").on(listeCours))
      .pause(3, 6)
      .exec(session -> session.set("code", "LT-" + session.userId() + "-" + ThreadLocalRandom.current().nextInt(1_000_000)))
      .exec(group("Ajouter un cours").on(
          exec(http("POST /courses").post("/courses").header("Authorization", AUTH)
              .body(StringBody("{\"code\":\"#{code}\",\"name\":\"Cours de test de charge\","
                  + "\"description\":\"Créé par Gatling, supprimé en fin de parcours\",\"capacity\":30}"))
              .check(jsonPath("$.id").saveAs("newCourseId")))
          .exec(listeCours)))
      .pause(3, 6)
      .exec(group("Modifier le cours").on(
          exec(http("PUT /courses/{id}").put("/courses/#{newCourseId}").header("Authorization", AUTH)
              .body(StringBody("{\"code\":\"#{code}\",\"name\":\"Cours de test de charge\","
                  + "\"description\":\"Créé par Gatling, supprimé en fin de parcours\",\"capacity\":40}")))
          .exec(listeCours)))
      .pause(2, 4)
      .exec(group("Supprimer le cours").on(
          exec(http("DELETE /courses/{id}").delete("/courses/#{newCourseId}").header("Authorization", AUTH))
          .exec(listeCours)))
      .pause(3, 6);

  // ---------- profils : utilisateurs simultanés au total, répartis 60/25/10/5 % ----------

  /** Paliers {secondes, utilisateurs simultanés visés} — mêmes valeurs que les profils k6. */
  static int[][] stages() {
    switch (PROFILE) {
      case "load":
        return new int[][] {{60, 20}, {300, 20}, {60, 0}};
      case "stress": {
        List<int[]> s = new ArrayList<>();
        for (int t : new int[] {10, 25, 50, 75, 100, 150}) {
          s.add(new int[] {30, t});
          s.add(new int[] {120, t});
        }
        s.add(new int[] {60, 0});
        return s.toArray(new int[0][]);
      }
      case "spike":
        return new int[][] {{10, 100}, {60, 100}, {20, 0}};
      default: // smoke
        return new int[][] {{10, 4}, {120, 4}};
    }
  }

  static ClosedInjectionStep[] injection(double share) {
    List<ClosedInjectionStep> steps = new ArrayList<>();
    int current = 0;
    for (int[] st : stages()) {
      int target = st[1] == 0 ? 0 : Math.max(1, (int) Math.round(st[1] * share));
      steps.add(target == current
          ? constantConcurrentUsers(target).during(st[0])
          : rampConcurrentUsers(current).to(target).during(st[0]));
      current = target;
    }
    return steps.toArray(new ClosedInjectionStep[0]);
  }

  {
    setUp(
        s1.injectClosed(injection(0.60)),
        s2.injectClosed(injection(0.25)),
        s3.injectClosed(injection(0.10)),
        s4.injectClosed(injection(0.05))
    ).protocols(httpProtocol)
        .assertions(
            global().responseTime().percentile(95.0).lt(1000),
            global().failedRequests().percent().lt(1.0));
  }
}
