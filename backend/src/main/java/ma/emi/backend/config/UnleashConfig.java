package ma.emi.backend.config;

import io.getunleash.DefaultUnleash;
import io.getunleash.Unleash;
import io.getunleash.util.UnleashConfig.Builder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UnleashConfig {

    @Bean(destroyMethod = "shutdown")
    public Unleash unleash(@Value("${unleash.api.url}") String apiUrl,
                           @Value("${unleash.api.token}") String apiToken,
                           @Value("${app.version}") String appVersion) {
        io.getunleash.util.UnleashConfig config = new Builder()
                .appName("gestcours-backend")
                .instanceId(appVersion)
                .unleashAPI(apiUrl)
                .apiKey(apiToken)
                // Polling court pour que le Kill Switch prenne effet en ~2 s
                .fetchTogglesInterval(2)
                .build();
        return new DefaultUnleash(config);
    }
}
