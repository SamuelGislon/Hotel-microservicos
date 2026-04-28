package com.hosped.api_gateway.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    @Autowired
    private JwtAuthFilter jwtAuthFilter;

    @Bean
    public RouteLocator rotas(RouteLocatorBuilder builder) {
        return builder.routes()

                .route("ms-pagamentos", r -> r
                        .path("/pagamentos/**")
                        .filters(f -> f.filter(jwtAuthFilter))
                        .uri("http://ms-pagamentos:8083"))

                .route("ms-reservas", r -> r
                        .path("/reservas/**")
                        .filters(f -> f.filter(jwtAuthFilter))
                        .uri("http://ms-reservas:8081"))

                .route("ms-hospedes", r -> r
                        .path("/hospedes/**")
                        .filters(f -> f.filter(jwtAuthFilter))
                        .uri("http://ms-hospedes:8082"))

                .route("ms-quartos", r -> r
                        .path("/quartos/**")
                        .filters(f -> f.filter(jwtAuthFilter))
                        .uri("http://ms-quartos:8084"))

                .route("ms-users-auth", r -> r
                        .path("/auth/**")
                        .uri("http://ms-users:8085"))

                .route("ms-users", r -> r
                        .path("/users/**")
                        .filters(f -> f.filter(jwtAuthFilter))
                        .uri("http://ms-users:8085"))

                .build();
    }
}