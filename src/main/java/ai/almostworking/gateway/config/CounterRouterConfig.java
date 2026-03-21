package ai.almostworking.gateway.config;

import ai.almostworking.gateway.handler.CounterHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static org.springframework.web.reactive.function.server.RequestPredicates.GET;

@Configuration
public class CounterRouterConfig {
    private final CounterHandler counterHandler;

    public CounterRouterConfig(CounterHandler counterHandler) {
        this.counterHandler = counterHandler;
    }

    @Bean
    public RouterFunction<ServerResponse> counterRoutes() {
        return route(GET("/api/counter"), counterHandler::getCounter);
    }
}
