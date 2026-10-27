package ma.emi.backend.controller;

import io.getunleash.FakeUnleash;
import ma.emi.backend.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("SimulationController - Feature Flag new-checkout-engine")
class SimulationControllerTest {

    private final FakeUnleash unleash = new FakeUnleash();
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new SimulationController(unleash, "v2.0.0-canary"))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("Flag ON -> 200 New Checkout Engine")
    void flagOn_newEngine() throws Exception {
        unleash.enable(SimulationController.FLAG);

        mockMvc.perform(get("/api/checkout"))
                .andExpect(status().isOk())
                .andExpect(header().string("X-App-Version", "v2.0.0-canary"))
                .andExpect(content().string("[v2.0.0-canary] New Checkout Engine executed successfully."));
    }

    @Test
    @DisplayName("Flag ON + triggerBug -> 500")
    void flagOn_triggerBug_crash() throws Exception {
        unleash.enable(SimulationController.FLAG);

        mockMvc.perform(get("/api/checkout").param("triggerBug", "true"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("Flag OFF + triggerBug -> 200 Legacy (Kill Switch)")
    void flagOff_triggerBug_legacy() throws Exception {
        unleash.disable(SimulationController.FLAG);

        mockMvc.perform(get("/api/checkout").param("triggerBug", "true"))
                .andExpect(status().isOk())
                .andExpect(content().string("[v2.0.0-canary] Legacy Checkout Engine executed successfully."));
    }
}
