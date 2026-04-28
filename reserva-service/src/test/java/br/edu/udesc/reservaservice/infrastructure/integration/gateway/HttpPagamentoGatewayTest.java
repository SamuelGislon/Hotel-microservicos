package br.edu.udesc.reservaservice.infrastructure.integration.gateway;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import br.edu.udesc.reservaservice.infrastructure.integration.client.PagamentoServiceHttpClient;
import br.edu.udesc.reservaservice.infrastructure.integration.fallback.PagamentoGatewayFallback;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;
import java.util.UUID;
import java.util.concurrent.Executor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class HttpPagamentoGatewayTest {

    @Mock
    private PagamentoServiceHttpClient pagamentoServiceHttpClient;

    @Test
    void deveAcionarFallbackQuandoPagamentoServiceFalhar() {
        UUID reservaId = UUID.randomUUID();
        when(pagamentoServiceHttpClient.consultarStatus(reservaId))
            .thenThrow(new RuntimeException("erro remoto"));

        HttpPagamentoGateway gateway = new HttpPagamentoGateway(
            pagamentoServiceHttpClient,
            new PagamentoGatewayFallback(),
            CircuitBreakerRegistry.ofDefaults(),
            TimeLimiterRegistry.ofDefaults(),
            executorSincrono()
        );

        StatusPagamentoIntegracao retorno = gateway.consultarStatusReserva(reservaId);

        assertThat(retorno.fallbackAcionado()).isTrue();
        assertThat(retorno.statusExterno()).isEqualTo("FALLBACK");
    }

    private Executor executorSincrono() {
        return Runnable::run;
    }
}
