package ai.almostworking.gateway.handler;

import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.Map;

@Component
public class CounterHandler {
    
    private final ReactiveRedisTemplate<String, Long> redisTemplate;

    public CounterHandler(ReactiveRedisTemplate<String, Long> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public Mono<ServerResponse> getCounter(ServerRequest request) {
        return redisTemplate.keys("spring:session:sessions:*")
            .count()
            .map(count -> Map.of("uniqueSessions", count))
            .flatMap(result -> ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(result));
    }
}
