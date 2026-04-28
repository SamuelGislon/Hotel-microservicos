package br.edu.udesc.reservaservice.api.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record ReservaComentarioResponse(
    UUID id,
    UUID reservaId,
    String comentario,
    LocalDateTime criadoAt
) {
}
