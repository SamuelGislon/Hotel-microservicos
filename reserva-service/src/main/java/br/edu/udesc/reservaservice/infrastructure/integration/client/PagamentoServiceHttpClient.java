package br.edu.udesc.reservaservice.infrastructure.integration.client;

import br.edu.udesc.reservaservice.infrastructure.config.AppProperties;
import br.edu.udesc.reservaservice.infrastructure.integration.gateway.StatusPagamentoIntegracao;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
@RequiredArgsConstructor
public class PagamentoServiceHttpClient {

    private final WebClient webClient;
    private final AppProperties appProperties;

    public StatusPagamentoIntegracao consultarStatus(UUID reservaId) {
        return webClient.get()
            .uri(appProperties.getIntegracao().getPagamento().getBaseUrl()
                + "/pagamentos/reserva/" + reservaId + "/status-integracao")
            .retrieve()
            .bodyToMono(StatusPagamentoIntegracao.class)
            .block();
    }

    public void confirmarPagamentoReserva(UUID reservaId) {
        webClient.post()
            .uri(appProperties.getIntegracao().getPagamento().getBaseUrl()
                + "/pagamentos/reserva/" + reservaId + "/confirmar")
            .retrieve()
            .bodyToMono(Void.class)
            .block();
    }
}
