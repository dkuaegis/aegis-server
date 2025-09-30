package aegis.server.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.session.config.SessionRepositoryCustomizer;
import org.springframework.session.data.redis.RedisIndexedSessionRepository;
import org.springframework.session.data.redis.RedisSessionMapper;

@Profile("!test")
@Configuration
public class RedisSessionSafetyConfig {

    @Bean
    public SessionRepositoryCustomizer<RedisIndexedSessionRepository> safeRedisSessionMapper() {
        return (repo) -> {
            RedisSessionMapper delegate = new RedisSessionMapper();
            repo.setRedisSessionMapper((sessionId, map) -> {
                try {
                    return delegate.apply(sessionId, map);
                } catch (IllegalStateException ex) {
                    return null; // 예외 발생 시 null 반환하여 세션 무시
                }
            });
        };
    }
}
