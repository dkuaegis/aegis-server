package aegis.server.global.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.OAuthFlow;
import io.swagger.v3.oas.models.security.OAuthFlows;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
public class SwaggerConfig {

    private OpenAPI baseOpenAPI() {
        return new OpenAPI()
                .info(new Info().title("Aegis Server API"))
                .addSecurityItem(new SecurityRequirement().addList("GoogleOAuth2"))
                .components(new io.swagger.v3.oas.models.Components()
                        .addSecuritySchemes(
                                "GoogleOAuth2",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.OAUTH2)
                                        .flows(new OAuthFlows()
                                                .authorizationCode(new OAuthFlow()
                                                        .authorizationUrl("/oauth2/authorization/google")))));
    }

    @Bean
    @Profile("dev")
    public OpenAPI devOpenAPI() {
        return baseOpenAPI().addServersItem(new Server().url("https://dev-api.dkuaegis.org"));
    }

    @Bean
    @Profile("local")
    public OpenAPI localOpenAPI() {
        return baseOpenAPI().addServersItem(new Server().url("http://localhost:8080"));
    }
}
