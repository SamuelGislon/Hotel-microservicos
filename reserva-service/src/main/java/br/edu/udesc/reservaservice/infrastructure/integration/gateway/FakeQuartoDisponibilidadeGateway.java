package br.edu.udesc.reservaservice.infrastructure.integration.gateway;

import java.time.LocalDate;
import java.util.UUID;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.integracao.quarto.strategy", havingValue = "fake", matchIfMissing = true)
public class FakeQuartoDisponibilidadeGateway implements QuartoDisponibilidadeGateway {

    @Override
    public DisponibilidadeQuarto verificarDisponibilidade(UUID quartoId, LocalDate checkIn, LocalDate checkOut) {
        return new DisponibilidadeQuarto(
            quartoId,
            true,
            false,
            "Validação local do quarto concluída em modo standalone"
        );
    }

    @Override
    public DisponibilidadeQuarto verificarDisponibilidadePorServico(Long quartoServicoId, LocalDate checkIn, LocalDate checkOut) {
        return new DisponibilidadeQuarto(
            quartoServicoId,
            null,
            true,
            false,
            "Validação local do quarto concluída em modo standalone"
        );
    }

    @Override
    public void registrarCheckOut(Long quartoServicoId) {
        // Sem integração externa no modo standalone.
    }
}
