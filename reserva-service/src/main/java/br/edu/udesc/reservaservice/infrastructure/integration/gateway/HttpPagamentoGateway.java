package br.edu.udesc.reservaservice.infrastructure.integration.gateway;

import br.edu.udesc.reservaservice.infrastructure.integration.client.PagamentoServiceHttpClient;
import br.edu.udesc.reservaservice.infrastructure.integration.fallback.PagamentoGatewayFallback;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.timelimiter.TimeLimiter;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Callable;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.integracao.pagamento.strategy", havingValue = "http")
public class HttpPagamentoGateway implements PagamentoGateway {

    private final PagamentoServiceHttpClient pagamentoServiceHttpClient;
    private final PagamentoGatewayFallback fallback;
    private final CircuitBreaker circuitBreaker;
    private final TimeLimiter timeLimiter;
    private final Executor integracaoExecutor;

    public HttpPagamentoGateway(
        PagamentoServiceHttpClient pagamentoServiceHttpClient,
        PagamentoGatewayFallback fallback,
        CircuitBreakerRegistry circuitBreakerRegistry,
        TimeLimiterRegistry timeLimiterRegistry,
        @Qualifier("integracaoExecutor") Executor integracaoExecutor
    ) {
        this.pagamentoServiceHttpClient = pagamentoServiceHttpClient;
        this.fallback = fallback;
        this.circuitBreaker = circuitBreakerRegistry.circuitBreaker("pagamentoService");
        this.timeLimiter = timeLimiterRegistry.timeLimiter("pagamentoService");
        this.integracaoExecutor = integracaoExecutor;
    }

    @Override
    public StatusPagamentoIntegracao consultarStatusReserva(UUID reservaId) {
        Callable<StatusPagamentoIntegracao> callable = CircuitBreaker.decorateCallable(
            circuitBreaker,
            TimeLimiter.decorateFutureSupplier(
                timeLimiter,
                () -> CompletableFuture.supplyAsync(
                    () -> pagamentoServiceHttpClient.consultarStatus(reservaId),
                    integracaoExecutor
                )
            )
        );

        try {
            return callable.call();
        } catch (Exception exception) {
            return fallback.aplicar(reservaId, exception);
        }
    }
}
