package ai.almostworking.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Configuration
@EnableWebFluxSecurity
public class GatewaySecConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .authorizeExchange(exchange -> exchange
                        .pathMatchers("/greetings").permitAll()
                        .pathMatchers("/hello").hasAnyRole("manage-account","view-profile")
//                        .pathMatchers("/hello").hasAnyRole("pizdetz")
                        .anyExchange().authenticated()
                )
//                .oauth2Client(Customizer.withDefaults())
                .oauth2Login(Customizer.withDefaults())
//                .oauth2Login(oauth2 -> oauth2.authenticationSuccessHandler(
//                        (webFilterExchange, authentication) -> {
//                            return webFilterExchange.getExchange().getResponse().setComplete();
//                        }
//                ))
//                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> {}))
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt ->
                        jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())))
                .build();
    }


    // this Bean is for Microservices wanting to go through this Gateway
    @Bean
    ReactiveJwtAuthenticationConverter jwtAuthenticationConverter() {
        var converter = new ReactiveJwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            Collection<GrantedAuthority> authorities = new ArrayList<>();

            Map<String, Object> realmAccess = jwt.getClaim("realm_access");
            if (realmAccess != null) {
                List<String> roles = (List<String>) realmAccess.get("roles");
                if (roles != null) {
                    roles.forEach(role ->
                            authorities.add(new SimpleGrantedAuthority("ROLE_" + role))
                    );
                }
            }
            Map<String, Object> resourceAccess = jwt.getClaim("resource_access");
            if (resourceAccess != null) {
                Map<String, Object> account =
                        (Map<String, Object>) resourceAccess.get("account");
                if (account != null) {
                    List<String> roles =
                            (List<String>) account.get("roles");
                    if (roles != null) {
                        roles.forEach(role ->
                                authorities.add(
                                        new SimpleGrantedAuthority("ROLE_" + role)
                                )
                        );
                    }

                }
            }

            return Flux.fromIterable(authorities);
        });
        return converter;
    }

}
