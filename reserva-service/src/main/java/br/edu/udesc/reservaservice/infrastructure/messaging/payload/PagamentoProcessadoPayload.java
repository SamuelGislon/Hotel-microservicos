package br.edu.udesc.reservaservice.infrastructure.messaging.payload;

import java.time.LocalDateTime;

public record PagamentoProcessadoPayload(
    String reservaId,
    String status,
    LocalDateTime dataAtualizacao
) {
}
