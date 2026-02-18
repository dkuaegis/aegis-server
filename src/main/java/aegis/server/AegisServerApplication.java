package aegis.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.metrics.buffering.BufferingApplicationStartup;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing(dateTimeProviderRef = "auditingDateTimeProvider")
@SpringBootApplication
public class AegisServerApplication {

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(AegisServerApplication.class);
        application.setApplicationStartup(new BufferingApplicationStartup(10000));
        application.run(args);
    }
}
