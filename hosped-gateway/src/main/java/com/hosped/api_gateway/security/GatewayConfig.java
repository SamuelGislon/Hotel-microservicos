package com.hosped.api_gateway.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    @Autowired
    private JwtAuthFilter jwtAuthFilter;

    @Value("${services.pagamentos-url}")
    private String pagamentosUrl;

    @Value("${services.reservas-url}")
    private String reservasUrl;

    @Value("${services.quartos-url}")
    private String quartosUrl;

    @Value("${services.users-url}")
    private String usersUrl;

    @Bean
    public RouteLocator rotas(RouteLocatorBuilder builder) {
        return builder.routes()

                .route("hosped-pagamento-confirmacao-publica", r -> r
                        .path("/pagamentos/pagar/**")
                        .uri(pagamentosUrl))

                .route("hosped-pagamento", r -> r
                        .path("/pagamentos/**")
                        .filters(f -> f.filter(jwtAuthFilter))
                        .uri(pagamentosUrl))

                .route("reserva-service-reservas", r -> r
                        .path("/api/v1/reservas/**")
                        .filters(f -> f.filter(jwtAuthFilter))
                        .uri(reservasUrl))

                .route("reserva-service-hospedes", r -> r
                        .path("/api/v1/hospedes/**")
                        .filters(f -> f.filter(jwtAuthFilter))
                        .uri(reservasUrl))

                .route("reserva-service-tecnico", r -> r
                        .path("/api/v1/tecnico/**")
                        .filters(f -> f.filter(jwtAuthFilter))
                        .uri(reservasUrl))

                .route("hosped-quarto", r -> r
                        .path("/quartos/**")
                        .filters(f -> f.filter(jwtAuthFilter))
                        .uri(quartosUrl))

                .route("hosped-users-auth", r -> r
                        .path("/auth/**")
                        .uri(usersUrl))

                .route("hosped-users-alterar-senha", r -> r
                        .path("/users/*/senha")
                        .filters(f -> f.filter(jwtAuthFilter))
                        .uri(usersUrl))

                .route("hosped-users", r -> r
                        .path("/users/**")
                        .filters(f -> f.filter(jwtAuthFilter.exigirCargo("ADMINISTRADOR")))
                        .uri(usersUrl))

                .build();
    }
}
