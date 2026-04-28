package br.edu.udesc.reservaservice.infrastructure.integration.gateway;

import br.edu.udesc.reservaservice.infrastructure.integration.client.QuartoServiceHttpClient;
import br.edu.udesc.reservaservice.infrastructure.integration.client.QuartoServicoResponse;
import br.edu.udesc.reservaservice.infrastructure.integration.fallback.QuartoDisponibilidadeFallback;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.timelimiter.TimeLimiter;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;
import java.time.LocalDate;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Callable;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.integracao.quarto.strategy", havingValue = "http")
public class HttpQuartoDisponibilidadeGateway implements QuartoDisponibilidadeGateway {

    private final QuartoServiceHttpClient quartoServiceHttpClient;
    private final QuartoDisponibilidadeFallback fallback;
    private final CircuitBreaker circuitBreaker;
    private final TimeLimiter timeLimiter;
    private final Executor integracaoExecutor;

    public HttpQuartoDisponibilidadeGateway(
        QuartoServiceHttpClient quartoServiceHttpClient,
        QuartoDisponibilidadeFallback fallback,
        CircuitBreakerRegistry circuitBreakerRegistry,
        TimeLimiterRegistry timeLimiterRegistry,
        @Qualifier("integracaoExecutor") Executor integracaoExecutor
    ) {
        this.quartoServiceHttpClient = quartoServiceHttpClient;
        this.fallback = fallback;
        this.circuitBreaker = circuitBreakerRegistry.circuitBreaker("quartoDisponibilidade");
        this.timeLimiter = timeLimiterRegistry.timeLimiter("quartoDisponibilidade");
        this.integracaoExecutor = integracaoExecutor;
    }

    @Override
    public DisponibilidadeQuarto verificarDisponibilidade(UUID quartoId, LocalDate checkIn, LocalDate checkOut) {
        Callable<DisponibilidadeQuarto> callable = CircuitBreaker.decorateCallable(
            circuitBreaker,
            TimeLimiter.decorateFutureSupplier(
                timeLimiter,
                () -> CompletableFuture.supplyAsync(
                    () -> quartoServiceHttpClient.consultarDisponibilidade(quartoId),
                    integracaoExecutor
                )
            )
        );

        try {
            return callable.call();
        } catch (Exception exception) {
            return fallback.aplicar(quartoId, exception);
        }
    }

    @Override
    public DisponibilidadeQuarto verificarDisponibilidadePorServico(Long quartoServicoId, LocalDate checkIn, LocalDate checkOut) {
        Callable<DisponibilidadeQuarto> callable = CircuitBreaker.decorateCallable(
            circuitBreaker,
            TimeLimiter.decorateFutureSupplier(
                timeLimiter,
                () -> CompletableFuture.supplyAsync(
                    () -> mapearDisponibilidade(quartoServiceHttpClient.buscarPorId(quartoServicoId)),
                    integracaoExecutor
                )
            )
        );

        try {
            return callable.call();
        } catch (Exception exception) {
            return fallback.aplicar(quartoServicoId, exception);
        }
    }

    @Override
    public void registrarCheckOut(Long quartoServicoId) {
        quartoServiceHttpClient.registrarCheckOut(quartoServicoId);
    }

    private DisponibilidadeQuarto mapearDisponibilidade(QuartoServicoResponse quarto) {
        boolean disponivel = "DISPONIVEL".equalsIgnoreCase(quarto.status());
        String numero = quarto.numeroQuarto() != null ? String.valueOf(quarto.numeroQuarto()) : null;
        String mensagem = disponivel
            ? "Quarto disponível no serviço de quartos"
            : "Quarto não disponível no serviço de quartos. Status atual: " + quarto.status();
        return new DisponibilidadeQuarto(quarto.id(), numero, disponivel, false, mensagem);
    }
}
