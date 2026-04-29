package com.user.api_users.exception;

import java.time.OffsetDateTime;

public record ErroResponse(
        OffsetDateTime timestamp,
        int status,
        String erro,
        String mensagem,
        String path
) {
}
