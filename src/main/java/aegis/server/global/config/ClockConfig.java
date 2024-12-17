package aegis.server.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

@Configuration
public class ClockConfig {

    @Value("${time.zone}")
    private String zoneId;

    @Bean
    public Clock clock() {
        return Clock.system(ZoneId.of(zoneId));
    }

}
