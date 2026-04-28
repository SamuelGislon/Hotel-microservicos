package br.edu.udesc.reservaservice.api.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegistrarComentarioReservaRequest(
    @NotBlank(message = "O comentário é obrigatório")
    @Size(max = 1000, message = "O comentário deve ter no máximo 1000 caracteres")
    String comentario
) {
}
