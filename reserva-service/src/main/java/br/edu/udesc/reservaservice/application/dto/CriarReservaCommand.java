package br.edu.udesc.reservaservice.application.dto;

import br.edu.udesc.reservaservice.domain.enums.MetodoPagamento;
import br.edu.udesc.reservaservice.domain.enums.PagamentoModo;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record CriarReservaCommand(
    UUID hospedeId,
    UUID quartoId,
    Long quartoServicoId,
    String quartoNumero,
    LocalDate checkInData,
    LocalDate checkOutData,
    PagamentoModo pagamentoModo,
    BigDecimal valorDiaria,
    MetodoPagamento metodoPagamento
) {
}
