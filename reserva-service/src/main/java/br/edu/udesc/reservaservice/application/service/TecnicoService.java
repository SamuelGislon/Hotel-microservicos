package br.edu.udesc.reservaservice.application.service;

import br.edu.udesc.reservaservice.application.dto.DisponibilidadeQuartoDto;
import br.edu.udesc.reservaservice.application.dto.StatusPagamentoIntegracaoDto;
import br.edu.udesc.reservaservice.application.mapper.ReservaMapper;
import br.edu.udesc.reservaservice.infrastructure.integration.client.SimuladorServicosExternosState;
import br.edu.udesc.reservaservice.infrastructure.integration.gateway.PagamentoGateway;
import br.edu.udesc.reservaservice.infrastructure.integration.gateway.QuartoDisponibilidadeGateway;
import br.edu.udesc.reservaservice.infrastructure.messaging.producer.IntegracaoPagamentoEventProducer;
import java.time.LocalDate;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TecnicoService {

    private final QuartoDisponibilidadeGateway quartoDisponibilidadeGateway;
    private final PagamentoGateway pagamentoGateway;
    private final SimuladorServicosExternosState simuladorServicosExternosState;
    private final IntegracaoPagamentoEventProducer integracaoPagamentoEventProducer;
    private final ReservaMapper reservaMapper;

    public DisponibilidadeQuartoDto consultarDisponibilidadeQuarto(UUID quartoId, LocalDate checkIn, LocalDate checkOut) {
        return reservaMapper.toDto(
            quartoDisponibilidadeGateway.verificarDisponibilidade(quartoId, checkIn, checkOut)
        );
    }

    public StatusPagamentoIntegracaoDto consultarStatusPagamento(UUID reservaId) {
        return reservaMapper.toDto(pagamentoGateway.consultarStatusReserva(reservaId));
    }

    public boolean alternarIndisponibilidadeQuarto(boolean ativo) {
        simuladorServicosExternosState.setQuartoIndisponivel(ativo);
        return simuladorServicosExternosState.isQuartoIndisponivel();
    }

    public boolean alternarIndisponibilidadePagamento(boolean ativo) {
        simuladorServicosExternosState.setPagamentoIndisponivel(ativo);
        return simuladorServicosExternosState.isPagamentoIndisponivel();
    }

    public void simularEventoPagamentoConfirmado(UUID reservaId) {
        integracaoPagamentoEventProducer.publicarConfirmacaoPagamento(reservaId);
    }
}
