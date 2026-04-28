package br.edu.udesc.reservaservice.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("reserva-service")
                .description("""
                    Microsserviço responsável por hóspedes e reservas.
                    Demonstra Event-Driven com RabbitMQ, Circuit Breaker com Resilience4j,
                    integração futura com quarto-service e pagamento-service e execução standalone.
                    """)
                .version("v1")
                .contact(new Contact()
                    .name("Projeto Acadêmico Profissional")
                    .email("arquitetura@udesc.br"))
                .license(new License()
                    .name("Uso acadêmico")
                    .url("https://opensource.org/licenses/MIT")));
    }
}
