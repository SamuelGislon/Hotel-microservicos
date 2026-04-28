package br.edu.udesc.reservaservice.application.mapper;

import br.edu.udesc.reservaservice.application.dto.DisponibilidadeQuartoDto;
import br.edu.udesc.reservaservice.application.dto.ReservaComentarioDto;
import br.edu.udesc.reservaservice.application.dto.ReservaDto;
import br.edu.udesc.reservaservice.application.dto.StatusPagamentoIntegracaoDto;
import br.edu.udesc.reservaservice.domain.model.Reserva;
import br.edu.udesc.reservaservice.domain.model.ReservaComentario;
import br.edu.udesc.reservaservice.infrastructure.integration.gateway.DisponibilidadeQuarto;
import br.edu.udesc.reservaservice.infrastructure.integration.gateway.StatusPagamentoIntegracao;
import org.springframework.stereotype.Component;

@Component
public class ReservaMapper {

    public ReservaDto toDto(Reserva reserva) {
        return new ReservaDto(
            reserva.getId(),
            reserva.getHospede().getId(),
            reserva.getHospede().getNomeCompleto(),
            reserva.getQuartoId(),
            reserva.getQuartoServicoId(),
            reserva.getQuartoNumero(),
            reserva.getCheckInData(),
            reserva.getCheckOutData(),
            reserva.getReservaStatus(),
            reserva.getPagamentoModo(),
            reserva.getPagamentoStatus(),
            reserva.getValorDiaria(),
            reserva.getMetodoPagamento(),
            reserva.getCriadoAt(),
            reserva.getAtualizadoAt(),
            reserva.getCheckInRealizadoAt(),
            reserva.getCheckOutRealizadoAt()
        );
    }

    public ReservaComentarioDto toDto(ReservaComentario comentario) {
        return new ReservaComentarioDto(
            comentario.getId(),
            comentario.getReserva().getId(),
            comentario.getComentario(),
            comentario.getCriadoAt()
        );
    }

    public DisponibilidadeQuartoDto toDto(DisponibilidadeQuarto disponibilidadeQuarto) {
        return new DisponibilidadeQuartoDto(
            disponibilidadeQuarto.quartoId(),
            disponibilidadeQuarto.disponivel(),
            disponibilidadeQuarto.fallbackAcionado(),
            disponibilidadeQuarto.mensagem()
        );
    }

    public StatusPagamentoIntegracaoDto toDto(StatusPagamentoIntegracao statusPagamentoIntegracao) {
        return new StatusPagamentoIntegracaoDto(
            statusPagamentoIntegracao.reservaId(),
            statusPagamentoIntegracao.statusExterno(),
            statusPagamentoIntegracao.fallbackAcionado(),
            statusPagamentoIntegracao.mensagem()
        );
    }
}
