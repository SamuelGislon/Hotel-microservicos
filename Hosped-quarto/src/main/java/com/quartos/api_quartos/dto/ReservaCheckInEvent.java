package com.quartos.api_quartos.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDateTime;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ReservaCheckInEvent(
        UUID eventId,
        String eventType,
        LocalDateTime occurredAt,
        UUID reservaId,
        UUID hospedeId,
        UUID quartoId,
        Long quartoServicoId,
        String reservaStatus,
        String pagamentoModo,
        String pagamentoStatus
) {
}
