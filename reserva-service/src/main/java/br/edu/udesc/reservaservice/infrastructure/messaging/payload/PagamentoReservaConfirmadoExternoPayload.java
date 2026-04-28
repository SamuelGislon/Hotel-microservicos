package br.edu.udesc.reservaservice.infrastructure.messaging.payload;

import java.time.LocalDateTime;
import java.util.UUID;

public record PagamentoReservaConfirmadoExternoPayload(
    UUID eventId,
    String eventType,
    LocalDateTime occurredAt,
    UUID reservaId
) {
}
