package ai.almostworking.gateway.config;

import ai.almostworking.gateway.filters.BlogVisitCounterFilterFactory;
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

    @Value("${be.blog.url}")
    private String beBlog;

    @Value("${api.url}")
    private String apiUrl;

    @Value("${fe.url}")
    private String feUrl;

    @Value("${fe.blog.url}")
    private String feBlogUrl;

    @Value("${be.storage.url}")
    private String minioStorageUrl;

    @Bean
    public RouteLocator myRoutes(RouteLocatorBuilder builder, 
                                  TokenRelayGatewayFilterFactory tokenRelay,
                                  BlogVisitCounterFilterFactory blogVisitCounter) {
        return builder.routes()
                .route("minio-greetings", p -> p.path("/api/greetings")
                        .uri(beMinio))
                .route("minio-hello", p -> p.path("/api/hello")
                        .filters(f -> f.filter(tokenRelay.apply()))
                        .uri(beMinio))
                .route("minio-bff", p -> p.path("/api/bff/**")
                        .filters(f -> f.filter(tokenRelay.apply()))
                        .uri(beMinio))
                .route("minio-all", p -> p.path("/api/minio/**")
                        .filters(f -> f.filter(tokenRelay.apply()))
                        .uri(beMinio))
                .route("blog-be", p -> p.path("/api/blog/**")
//                        .filters(f -> f.rewritePath("/api/blog/(?<path>.*)", "/${path}"))
                        .uri(beBlog))

                .route("blog-fe", p -> p.host("blog.s4v3.net", "blog.s4v3.local", "localhost")
//                        .filters(f -> f.filter(blogVisitCounter.apply(c -> {})))
                        .uri(feBlogUrl))

                .route("storage-signature", p -> p.query("X-Amz-Signature")
                        .uri(minioStorageUrl))

                .route("frontend", p -> p.path("/**")
//                        .filters(f -> f.filter(blogVisitCounter.apply(c -> {})))
                        .uri(feUrl))
                .build();
    }
}
