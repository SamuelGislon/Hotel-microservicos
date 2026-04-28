package br.edu.udesc.reservaservice.infrastructure.integration.fallback;

import br.edu.udesc.reservaservice.infrastructure.config.AppProperties;
import br.edu.udesc.reservaservice.infrastructure.integration.gateway.DisponibilidadeQuarto;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class QuartoDisponibilidadeFallback {

    private final AppProperties appProperties;

    public DisponibilidadeQuarto aplicar(UUID quartoId, Throwable throwable) {
        boolean reservaPermitida = appProperties.getIntegracao().getQuarto().isPermitirReservaSemValidacaoExterna();
        String mensagem = reservaPermitida
            ? "Fallback acionado: validação externa indisponível, reserva permitida em modo standalone"
            : "Fallback acionado: validação externa indisponível, reserva bloqueada por segurança";
        return new DisponibilidadeQuarto(quartoId, reservaPermitida, true, mensagem);
    }

    public DisponibilidadeQuarto aplicar(Long quartoServicoId, Throwable throwable) {
        boolean reservaPermitida = appProperties.getIntegracao().getQuarto().isPermitirReservaSemValidacaoExterna();
        String mensagem = reservaPermitida
            ? "Fallback acionado: validação externa indisponível, reserva permitida em modo standalone"
            : "Fallback acionado: validação externa indisponível, reserva bloqueada por segurança";
        return new DisponibilidadeQuarto(quartoServicoId, null, reservaPermitida, true, mensagem);
    }
}
