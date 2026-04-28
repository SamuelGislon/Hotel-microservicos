package br.edu.udesc.reservaservice.api.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CriarHospedeRequest(
    @NotBlank(message = "Nome completo é obrigatório")
    @Size(max = 160, message = "Nome completo deve ter no máximo 160 caracteres")
    String nomeCompleto,

    @NotBlank(message = "CPF é obrigatório")
    @Pattern(regexp = "^[0-9.\\-]{11,14}$", message = "CPF deve conter 11 dígitos")
    String cpf,

    @NotBlank(message = "E-mail é obrigatório")
    @Email(message = "E-mail inválido")
    @Size(max = 160, message = "E-mail deve ter no máximo 160 caracteres")
    String email,

    @NotBlank(message = "Telefone é obrigatório")
    @Size(max = 30, message = "Telefone deve ter no máximo 30 caracteres")
    String telefone
) {
}
