package br.edu.udesc.reservaservice.domain.event;

import br.edu.udesc.reservaservice.domain.enums.MetodoPagamento;
import br.edu.udesc.reservaservice.domain.model.Reserva;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record PagamentoReservaCriadaEvent(
    UUID reservaId,
    String nomeHospede,
    String emailHospede,
    BigDecimal valorDiaria,
    LocalDate checkIn,
    LocalDate checkOut,
    MetodoPagamento metodoPagamento
) {

    public static PagamentoReservaCriadaEvent from(Reserva reserva) {
        return new PagamentoReservaCriadaEvent(
            reserva.getId(),
            reserva.getHospede().getNomeCompleto(),
            reserva.getHospede().getEmail(),
            reserva.getValorDiaria(),
            reserva.getCheckInData(),
            reserva.getCheckOutData(),
            reserva.getMetodoPagamento()
        );
    }
}
