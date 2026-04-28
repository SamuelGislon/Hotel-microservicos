package br.edu.udesc.reservaservice.infrastructure.messaging.payload;

import br.edu.udesc.reservaservice.domain.enums.PagamentoModo;
import br.edu.udesc.reservaservice.domain.enums.PagamentoStatus;
import br.edu.udesc.reservaservice.domain.enums.ReservaStatus;
import java.time.LocalDateTime;
import java.util.UUID;

public record ReservaEventoPayload(
    UUID eventId,
    String eventType,
    LocalDateTime occurredAt,
    UUID reservaId,
    UUID hospedeId,
    UUID quartoId,
    ReservaStatus reservaStatus,
    PagamentoModo pagamentoModo,
    PagamentoStatus pagamentoStatus
) {
}
