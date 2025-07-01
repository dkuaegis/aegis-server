package aegis.server.global.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
public class SwaggerConfig {

    private OpenAPI baseOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Aegis Server API"));
    }

    @Bean
    @Profile("dev")
    public OpenAPI devOpenAPI() {
        return baseOpenAPI()
                .addServersItem(new Server().url("https://dev-api.dkuaegis.org"));
    }

    @Bean
    @Profile("local")
    public OpenAPI localOpenAPI() {
        return baseOpenAPI()
                .addServersItem(new Server().url("http://localhost:8080"));
    }
}
