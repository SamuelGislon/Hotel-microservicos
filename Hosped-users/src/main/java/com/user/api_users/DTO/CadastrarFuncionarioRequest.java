package com.user.api_users.DTO;

import com.user.api_users.model.Cargos;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CadastrarFuncionarioRequest(
        @NotBlank(message = "Nome é obrigatório")
        @Size(max = 160, message = "Nome deve ter no máximo 160 caracteres")
        String nome,

        @NotBlank(message = "CPF é obrigatório")
        @Pattern(regexp = "^\\d{11}$", message = "CPF deve conter 11 dígitos numéricos")
        String cpf,

        @NotNull(message = "Cargo é obrigatório")
        Cargos cargo,

        @NotBlank(message = "Senha é obrigatória")
        String senha
) {
}
