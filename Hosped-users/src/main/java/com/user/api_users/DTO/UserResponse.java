package com.user.api_users.DTO;

import com.user.api_users.model.Cargos;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String nome,
        String cpf,
        Cargos cargo
) {
}
