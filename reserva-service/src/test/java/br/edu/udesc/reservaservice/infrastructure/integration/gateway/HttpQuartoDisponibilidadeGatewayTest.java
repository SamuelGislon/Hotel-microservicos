package br.edu.udesc.reservaservice.infrastructure.integration.gateway;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import br.edu.udesc.reservaservice.infrastructure.config.AppProperties;
import br.edu.udesc.reservaservice.infrastructure.integration.client.QuartoServiceHttpClient;
import br.edu.udesc.reservaservice.infrastructure.integration.client.QuartoServicoResponse;
import br.edu.udesc.reservaservice.infrastructure.integration.fallback.QuartoDisponibilidadeFallback;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;
import java.time.LocalDate;
import java.util.UUID;
import java.util.concurrent.Executor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class HttpQuartoDisponibilidadeGatewayTest {

    @Mock
    private QuartoServiceHttpClient quartoServiceHttpClient;

    @Test
    void deveAcionarFallbackQuandoClienteHttpFalhar() {
        UUID quartoId = UUID.randomUUID();
        AppProperties properties = new AppProperties();
        properties.getIntegracao().getQuarto().setPermitirReservaSemValidacaoExterna(true);

        when(quartoServiceHttpClient.consultarDisponibilidade(quartoId))
            .thenThrow(new RuntimeException("timeout"));

        HttpQuartoDisponibilidadeGateway gateway = new HttpQuartoDisponibilidadeGateway(
            quartoServiceHttpClient,
            new QuartoDisponibilidadeFallback(properties),
            CircuitBreakerRegistry.ofDefaults(),
            TimeLimiterRegistry.ofDefaults(),
            executorSincrono()
        );

        DisponibilidadeQuarto retorno = gateway.verificarDisponibilidade(quartoId, LocalDate.now(), LocalDate.now().plusDays(1));

        assertThat(retorno.fallbackAcionado()).isTrue();
        assertThat(retorno.disponivel()).isTrue();
    }

    @Test
    void deveMapearQuartoServicoDisponivel() {
        AppProperties properties = new AppProperties();
        when(quartoServiceHttpClient.buscarPorId(10L))
            .thenReturn(new QuartoServicoResponse(10L, 101, 2, "STANDARD", "DISPONIVEL"));

        HttpQuartoDisponibilidadeGateway gateway = new HttpQuartoDisponibilidadeGateway(
            quartoServiceHttpClient,
            new QuartoDisponibilidadeFallback(properties),
            CircuitBreakerRegistry.ofDefaults(),
            TimeLimiterRegistry.ofDefaults(),
            executorSincrono()
        );

        DisponibilidadeQuarto retorno = gateway.verificarDisponibilidadePorServico(
            10L,
            LocalDate.now(),
            LocalDate.now().plusDays(1)
        );

        assertThat(retorno.quartoServicoId()).isEqualTo(10L);
        assertThat(retorno.quartoNumero()).isEqualTo("101");
        assertThat(retorno.disponivel()).isTrue();
        assertThat(retorno.fallbackAcionado()).isFalse();
    }

    private Executor executorSincrono() {
        return Runnable::run;
    }
}
