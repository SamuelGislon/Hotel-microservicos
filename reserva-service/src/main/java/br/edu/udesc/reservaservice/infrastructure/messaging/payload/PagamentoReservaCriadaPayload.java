package br.edu.udesc.reservaservice.infrastructure.messaging.payload;

import br.edu.udesc.reservaservice.domain.enums.MetodoPagamento;
import java.math.BigDecimal;
import java.time.LocalDate;

public record PagamentoReservaCriadaPayload(
    String reservaId,
    String nomeHospede,
    String emailHospede,
    BigDecimal valorDiaria,
    LocalDate checkIn,
    LocalDate checkOut,
    MetodoPagamento metodoPagamento
) {
}
