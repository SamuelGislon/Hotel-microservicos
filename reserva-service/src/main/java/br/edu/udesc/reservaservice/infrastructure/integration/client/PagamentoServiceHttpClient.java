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
                + "/api/v1/tecnico/simulacoes/pagamento-service/reservas/" + reservaId + "/status")
            .retrieve()
            .bodyToMono(StatusPagamentoIntegracao.class)
            .block();
    }
}
