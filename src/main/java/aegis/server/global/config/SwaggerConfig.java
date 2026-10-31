package aegis.server.global.config;

import io.swagger.v3.core.jackson.ModelResolver;
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

    static {
        // 같은 enum이 여러 DTO에 쓰여도 하나의 스키마를 참조하도록 해서 생성된 클라이언트 타입이 중복되지 않게 합니다.
        ModelResolver.enumsAsRef = true;
    }

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
    @Profile("prod")
    public OpenAPI prodOpenAPI() {
        return baseOpenAPI().addServersItem(new Server().url("https://api.dkuaegis.org"));
    }

    @Bean
    @Profile("staging")
    public OpenAPI stagingOpenAPI() {
        return baseOpenAPI().addServersItem(new Server().url("https://staging-api.dkuaegis.org"));
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
