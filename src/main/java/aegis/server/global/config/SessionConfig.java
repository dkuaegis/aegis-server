package aegis.server.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisIndexedHttpSession;

@Profile("!test")
@Configuration
@EnableRedisIndexedHttpSession
public class SessionConfig {}
