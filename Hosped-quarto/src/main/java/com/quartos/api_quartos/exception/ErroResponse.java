package com.quartos.api_quartos.exception;

import java.time.OffsetDateTime;

public record ErroResponse(
        OffsetDateTime timestamp,
        int status,
        String erro,
        String mensagem,
        String path
) {
}
