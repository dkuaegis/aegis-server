package aegis.server.global.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
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

import lombok.RequiredArgsConstructor;

import aegis.server.global.security.oidc.CustomAuthenticationFailureHandler;
import aegis.server.global.security.oidc.CustomOidcUserService;
import aegis.server.global.security.oidc.CustomSuccessHandler;
import aegis.server.global.security.oidc.RefererFilter;

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
    public SecurityFilterChain securityFilterChain(HttpSecurity http) {
        http.cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable));

        http.exceptionHandling(exceptionHandling -> exceptionHandling.authenticationEntryPoint(
                (request, response, authException) -> response.setStatus(HttpStatus.UNAUTHORIZED.value())));

        http.authorizeHttpRequests(auth -> auth
                // 공개 API (인증 불필요)
                .requestMatchers("/actuator/**", "/internal/**", "/test/**", "/auth/error/**", "/docs/**")
                .permitAll()

                // 관리자 전용 API
                .requestMatchers("/admin/**")
                .hasRole("ADMIN")

                // 정회원 전용 API (개발용 포함)
                .requestMatchers("/dev/**", "/mypage", "/studies/**", "/points/**", "/qrcode/**", "/point-shop/**")
                .hasRole("USER")

                // 회원가입 과정 API (게스트 이상 접근 가능)
                .requestMatchers("/auth/check", "/members/**", "/survey", "/coupons/**", "/payments/**")
                .hasRole("GUEST")

                // 나머지 모든 요청은 인증 필요
                .anyRequest()
                .authenticated());

        http.logout(logout -> logout.logoutSuccessUrl("/"));

        http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.NEVER));

        http.oauth2Login(oauth2 -> oauth2.userInfoEndpoint(userInfo -> userInfo.oidcUserService(customOidcUserService))
                .successHandler(customSuccessHandler)
                .failureHandler(authenticationFailureHandler));

        http.addFilterBefore(refererFilter, OAuth2AuthorizationRequestRedirectFilter.class);

        return http.build();
    }

    @Bean
    public RoleHierarchy roleHierarchy() {
        return RoleHierarchyImpl.fromHierarchy("ROLE_ADMIN > ROLE_STAFF > ROLE_USER > ROLE_GUEST");
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
