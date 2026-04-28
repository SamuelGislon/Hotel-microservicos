package br.edu.udesc.reservaservice.api.response;

import br.edu.udesc.reservaservice.domain.enums.MetodoPagamento;
import br.edu.udesc.reservaservice.domain.enums.PagamentoModo;
import br.edu.udesc.reservaservice.domain.enums.PagamentoStatus;
import br.edu.udesc.reservaservice.domain.enums.ReservaStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record ReservaResponse(
    UUID id,
    UUID hospedeId,
    String nomeHospede,
    UUID quartoId,
    Long quartoServicoId,
    String quartoNumero,
    LocalDate checkInData,
    LocalDate checkOutData,
    ReservaStatus reservaStatus,
    PagamentoModo pagamentoModo,
    PagamentoStatus pagamentoStatus,
    BigDecimal valorDiaria,
    MetodoPagamento metodoPagamento,
    LocalDateTime criadoAt,
    LocalDateTime atualizadoAt,
    LocalDateTime checkInRealizadoAt,
    LocalDateTime checkOutRealizadoAt
) {
}
