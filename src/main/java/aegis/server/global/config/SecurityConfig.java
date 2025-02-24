package aegis.server.global.config;

import aegis.server.global.security.oidc.CustomAuthenticationFailureHandler;
import aegis.server.global.security.oidc.CustomOidcUserService;
import aegis.server.global.security.oidc.CustomSuccessHandler;
import aegis.server.global.security.oidc.RefererFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestRedirectFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

import static aegis.server.global.constant.Constant.ALLOWED_CLIENT_URLS;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomOidcUserService customOidcUserService;
    private final CustomSuccessHandler customSuccessHandler;
    private final CustomAuthenticationFailureHandler authenticationFailureHandler;
    private final RefererFilter refererFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .headers(headers -> headers
                        .frameOptions(HeadersConfigurer.FrameOptionsConfig::disable)
                );

        http.exceptionHandling(exceptionHandling -> exceptionHandling
                .authenticationEntryPoint((request, response, authException) -> {
                    response.setStatus(HttpStatus.UNAUTHORIZED.value());
                })
        );

        http.authorizeHttpRequests(auth -> auth
                .requestMatchers("/internal/transaction").permitAll()
                .requestMatchers("/test/**").permitAll()
                .requestMatchers("/auth/error/**").permitAll()
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
        );

        http.logout(logout -> logout.logoutSuccessUrl("/"));

        http.sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.NEVER)
        );

        http.oauth2Login(oauth2 -> oauth2
                .userInfoEndpoint(userInfo ->
                        userInfo.oidcUserService(customOidcUserService))
                .successHandler(customSuccessHandler)
                .failureHandler(authenticationFailureHandler)
        );

        http.addFilterBefore(refererFilter, OAuth2AuthorizationRequestRedirectFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(ALLOWED_CLIENT_URLS);
        configuration.setAllowedMethods(List.of("*"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setExposedHeaders(List.of("Set-Cookie"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
