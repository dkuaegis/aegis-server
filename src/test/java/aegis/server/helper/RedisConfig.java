package aegis.server.helper;

import java.io.IOException;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;

import com.github.fppt.jedismock.RedisServer;

@Configuration
public class RedisConfig {

    private RedisServer redisServer;

    @PostConstruct
    public void startRedisServer() {
        redisServer = RedisServer.newRedisServer();
        try {
            redisServer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @PreDestroy
    public void stopRedisServer() throws IOException {
        if (redisServer != null && redisServer.isRunning()) {
            redisServer.stop();
        }
    }

    @Bean(destroyMethod = "shutdown")
    public RedisClient redisClient() {
        RedisURI redisURI = RedisURI.create(redisServer.getHost(), redisServer.getBindPort());
        return RedisClient.create(redisURI);
    }

    @Bean()
    public RedisConnectionFactory redisConnectionFactory() {
        return new LettuceConnectionFactory(
                new RedisStandaloneConfiguration(
                        redisServer.getHost(),
                        redisServer.getBindPort()
                )
        );
    }
}
