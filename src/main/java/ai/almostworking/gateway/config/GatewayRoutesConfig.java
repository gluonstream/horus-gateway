package ai.almostworking.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.factory.TokenRelayGatewayFilterFactory;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayRoutesConfig {

    @Value("${be.minio.url}")
    private String beMinio;

    @Value("${api.url}")
    private String apiUrl;

    @Value("${be.storage.url}")
    private String minioStorageUrl;

    @Bean
    public RouteLocator myRoutes(RouteLocatorBuilder builder, TokenRelayGatewayFilterFactory tokenRelay) {
        return builder.routes()
                .route("minio-hello", p -> p.path("/hello")
                        .filters(f -> f.filter(tokenRelay.apply()))
                        .uri(beMinio))
                .route("minio-bff", p -> p.path("/bff/**")
                        .filters(f -> f.filter(tokenRelay.apply()))
                        .uri(beMinio))
                .route("minio-greetings", p -> p.path("/greetings")
                        .uri(beMinio))
                .route("minio-all", p -> p.path("/minio/**")
                        .filters(f -> f.filter(tokenRelay.apply()))
                        .uri(beMinio))
                .route("storage-signature", p -> p.query("X-Amz-Signature")
                        .uri(minioStorageUrl))
                .route("api-all", p -> p.path("/api/**")
                        .filters(f -> f.filter(tokenRelay.apply()))
                        .uri(apiUrl))
                .build();
    }
}
