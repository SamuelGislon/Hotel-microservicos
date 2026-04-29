package br.edu.udesc.reservaservice.domain.event;

import br.edu.udesc.reservaservice.domain.enums.PagamentoModo;
import br.edu.udesc.reservaservice.domain.enums.PagamentoStatus;
import br.edu.udesc.reservaservice.domain.enums.ReservaStatus;
import br.edu.udesc.reservaservice.domain.model.Reserva;
import java.time.LocalDateTime;
import java.util.UUID;

public record ReservaDomainEvent(
    UUID eventId,
    String eventType,
    LocalDateTime occurredAt,
    UUID reservaId,
    UUID hospedeId,
    UUID quartoId,
    Long quartoServicoId,
    ReservaStatus reservaStatus,
    PagamentoModo pagamentoModo,
    PagamentoStatus pagamentoStatus
) {

    public static ReservaDomainEvent reservaCriada(Reserva reserva) {
        return from("RESERVA_CRIADA", reserva);
    }

    public static ReservaDomainEvent checkInRealizado(Reserva reserva) {
        return from("CHECKIN_REALIZADO", reserva);
    }

    public static ReservaDomainEvent checkOutRealizado(Reserva reserva) {
        return from("CHECKOUT_REALIZADO", reserva);
    }

    public static ReservaDomainEvent pagamentoConfirmado(Reserva reserva) {
        return from("PAGAMENTO_RESERVA_CONFIRMADO", reserva);
    }

    private static ReservaDomainEvent from(String tipoEvento, Reserva reserva) {
        return new ReservaDomainEvent(
            UUID.randomUUID(),
            tipoEvento,
            LocalDateTime.now(),
            reserva.getId(),
            reserva.getHospede().getId(),
            reserva.getQuartoId(),
            reserva.getQuartoServicoId(),
            reserva.getReservaStatus(),
            reserva.getPagamentoModo(),
            reserva.getPagamentoStatus()
        );
    }
}
