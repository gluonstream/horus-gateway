package ai.almostworking.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {



    @Bean
    public ReactiveRedisTemplate<String, Long> reactiveRedisTemplate(
            ReactiveRedisConnectionFactory factory) {
        StringRedisSerializer keySerializer = new StringRedisSerializer();
        RedisSerializationContext<String, Long> context = RedisSerializationContext.<String, Long>newSerializationContext(keySerializer)
                .key(keySerializer)
                .value(new org.springframework.data.redis.serializer.GenericToStringSerializer<>(Long.class))
                .hashKey(keySerializer)
                .hashValue(new org.springframework.data.redis.serializer.GenericToStringSerializer<>(Long.class))
                .build();
        return new ReactiveRedisTemplate<>(factory, context);
    }
}
