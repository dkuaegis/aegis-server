package aegis.server.global.config;

import tools.jackson.databind.jsontype.BasicPolymorphicTypeValidator;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisKeyValueAdapter;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.security.jackson.SecurityJacksonModules;

@Configuration
@EnableRedisRepositories(enableKeyspaceEvents = RedisKeyValueAdapter.EnableKeyspaceEvents.ON_STARTUP)
@Profile("!test")
public class RedisConfig {

    @Bean
    public RedisSerializer<Object> redisValueSerializer() {
        BasicPolymorphicTypeValidator.Builder typeValidatorBuilder =
                BasicPolymorphicTypeValidator.builder().allowIfSubType(Object.class);

        return GenericJacksonJsonRedisSerializer.builder()
                .customize(mapperBuilder -> mapperBuilder.addModules(
                        SecurityJacksonModules.getModules(getClass().getClassLoader(), typeValidatorBuilder)))
                .build();
    }

    // Spring Session이 기본 값 직렬화기로 사용
    @Bean(name = "springSessionDefaultRedisSerializer")
    public RedisSerializer<Object> springSessionDefaultRedisSerializer(RedisSerializer<Object> redisValueSerializer) {
        return redisValueSerializer;
    }

    @Bean
    @Primary
    public RedisTemplate<Object, Object> redisTemplate(
            RedisConnectionFactory connectionFactory, RedisSerializer<Object> redisValueSerializer) {
        RedisTemplate<Object, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        StringRedisSerializer stringSerializer = new StringRedisSerializer();

        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);
        template.setValueSerializer(redisValueSerializer);
        template.setHashValueSerializer(redisValueSerializer);

        template.afterPropertiesSet();
        return template;
    }
}
