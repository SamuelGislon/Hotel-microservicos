package br.edu.udesc.reservaservice.infrastructure.integration.client;

import br.edu.udesc.reservaservice.infrastructure.config.AppProperties;
import br.edu.udesc.reservaservice.infrastructure.integration.gateway.DisponibilidadeQuarto;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
@RequiredArgsConstructor
public class QuartoServiceHttpClient {

    private final WebClient webClient;
    private final AppProperties appProperties;

    public DisponibilidadeQuarto consultarDisponibilidade(UUID quartoId) {
        return webClient.get()
            .uri(appProperties.getIntegracao().getQuarto().getBaseUrl()
                + "/quartos/integracao/" + quartoId + "/disponibilidade")
            .retrieve()
            .bodyToMono(DisponibilidadeQuarto.class)
            .block();
    }

    public QuartoServicoResponse buscarPorId(Long quartoServicoId) {
        return webClient.get()
            .uri(appProperties.getIntegracao().getQuarto().getBaseUrl() + "/quartos/" + quartoServicoId)
            .retrieve()
            .bodyToMono(QuartoServicoResponse.class)
            .block();
    }

    public void registrarCheckOut(Long quartoServicoId) {
        webClient.patch()
            .uri(appProperties.getIntegracao().getQuarto().getBaseUrl() + "/quartos/" + quartoServicoId + "/checkout")
            .retrieve()
            .bodyToMono(QuartoServicoResponse.class)
            .block();
    }
}
