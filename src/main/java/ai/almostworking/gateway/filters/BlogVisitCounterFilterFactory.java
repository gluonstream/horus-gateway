package ai.almostworking.gateway.filters;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class BlogVisitCounterFilterFactory
        extends AbstractGatewayFilterFactory<BlogVisitCounterFilterFactory.Config> {

    private static final String GLOBAL_KEY = "gateway:counter:total";
    private static final String SESSION_KEY_PREFIX = "gateway:counter:session:";

    private final ReactiveRedisTemplate<String, Long> redisTemplate;

    public BlogVisitCounterFilterFactory(ReactiveRedisTemplate<String, Long> redisTemplate) {
        super(Config.class);
        this.redisTemplate = redisTemplate;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
//            String path = exchange.getRequest().getPath().value();

            return exchange.getSession()
                    .flatMap(session -> {
                        String sessionId = session.getId();
                        return redisTemplate.opsForValue().increment(GLOBAL_KEY, 1)
                                .then(redisTemplate.opsForValue().increment(SESSION_KEY_PREFIX + sessionId, 1));
                    })
                    .then(chain.filter(exchange));
        };
    }

    public static class Config {
    }
}
