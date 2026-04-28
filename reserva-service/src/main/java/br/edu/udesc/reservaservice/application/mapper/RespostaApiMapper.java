package br.edu.udesc.reservaservice.application.mapper;

import br.edu.udesc.reservaservice.api.response.DisponibilidadeQuartoResponse;
import br.edu.udesc.reservaservice.api.response.HospedeResponse;
import br.edu.udesc.reservaservice.api.response.ReservaComentarioResponse;
import br.edu.udesc.reservaservice.api.response.ReservaResponse;
import br.edu.udesc.reservaservice.api.response.StatusPagamentoIntegracaoResponse;
import br.edu.udesc.reservaservice.application.dto.DisponibilidadeQuartoDto;
import br.edu.udesc.reservaservice.application.dto.HospedeDto;
import br.edu.udesc.reservaservice.application.dto.ReservaComentarioDto;
import br.edu.udesc.reservaservice.application.dto.ReservaDto;
import br.edu.udesc.reservaservice.application.dto.StatusPagamentoIntegracaoDto;
import org.springframework.stereotype.Component;

@Component
public class RespostaApiMapper {

    public HospedeResponse toResponse(HospedeDto dto) {
        return new HospedeResponse(
            dto.id(),
            dto.nomeCompleto(),
            dto.cpf(),
            dto.email(),
            dto.telefone(),
            dto.ativo(),
            dto.criadoAt(),
            dto.atualizadoAt()
        );
    }

    public ReservaResponse toResponse(ReservaDto dto) {
        return new ReservaResponse(
            dto.id(),
            dto.hospedeId(),
            dto.nomeHospede(),
            dto.quartoId(),
            dto.quartoServicoId(),
            dto.quartoNumero(),
            dto.checkInData(),
            dto.checkOutData(),
            dto.reservaStatus(),
            dto.pagamentoModo(),
            dto.pagamentoStatus(),
            dto.valorDiaria(),
            dto.metodoPagamento(),
            dto.criadoAt(),
            dto.atualizadoAt(),
            dto.checkInRealizadoAt(),
            dto.checkOutRealizadoAt()
        );
    }

    public ReservaComentarioResponse toResponse(ReservaComentarioDto dto) {
        return new ReservaComentarioResponse(
            dto.id(),
            dto.reservaId(),
            dto.comentario(),
            dto.criadoAt()
        );
    }

    public DisponibilidadeQuartoResponse toResponse(DisponibilidadeQuartoDto dto) {
        return new DisponibilidadeQuartoResponse(
            dto.quartoId(),
            dto.disponivel(),
            dto.fallbackAcionado(),
            dto.mensagem()
        );
    }

    public StatusPagamentoIntegracaoResponse toResponse(StatusPagamentoIntegracaoDto dto) {
        return new StatusPagamentoIntegracaoResponse(
            dto.reservaId(),
            dto.statusExterno(),
            dto.fallbackAcionado(),
            dto.mensagem()
        );
    }
}
