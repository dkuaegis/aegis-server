package aegis.server.global.config;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import redis.embedded.RedisServer;

import java.io.IOException;

@Slf4j
@Configuration
@Profile({"test"})
public class EmbeddedRedisConfig {

    private RedisServer redisServer;

    @PostConstruct
    public void redisServer() throws IOException {
        redisServer = new RedisServer(6379);
        try {
            redisServer.start();
        } catch (Exception e) {
            log.error("Embedded Redis 시작 실패: ", e);
        }
    }

    @PreDestroy
    public void stopRedis() throws IOException {
        if (redisServer != null) {
            redisServer.stop();
        }
    }

}
