package ma.emi.backend.controller;

import io.getunleash.Unleash;
import lombok.extern.slf4j.Slf4j;
import ma.emi.backend.exception.SimulatedCrashException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/checkout")
public class SimulationController {
    static final String FLAG = "new-checkout-engine";

    private final Unleash unleash;
    private final String appVersion;

    public SimulationController(Unleash unleash, @Value("${app.version}") String appVersion) {
        this.unleash = unleash;
        this.appVersion = appVersion;
    }

    @GetMapping
    public ResponseEntity<String> checkout(@RequestParam(defaultValue = "false") boolean triggerBug) {
        boolean enabled = unleash.isEnabled(FLAG);
        log.info("Checking feature flag '{}': {}", FLAG, enabled);

        if (!enabled) {
            return reply("[" + appVersion + "] Legacy Checkout Engine executed successfully.");
        }
        if (triggerBug) {
            log.error("Simulated crash triggered in new checkout engine! App version: {}", appVersion);
            throw new SimulatedCrashException(appVersion);
        }
        return reply("[" + appVersion + "] New Checkout Engine executed successfully.");
    }

    private ResponseEntity<String> reply(String body) {
        return ResponseEntity.ok().header("X-App-Version", appVersion).body(body);
    }
}
