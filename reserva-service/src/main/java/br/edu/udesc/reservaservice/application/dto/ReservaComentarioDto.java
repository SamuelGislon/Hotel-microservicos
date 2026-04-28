package br.edu.udesc.reservaservice.application.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record ReservaComentarioDto(
    UUID id,
    UUID reservaId,
    String comentario,
    LocalDateTime criadoAt
) {
}
