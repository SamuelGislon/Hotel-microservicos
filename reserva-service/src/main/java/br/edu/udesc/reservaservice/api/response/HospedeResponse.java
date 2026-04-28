package br.edu.udesc.reservaservice.api.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record HospedeResponse(
    UUID id,
    String nomeCompleto,
    String cpf,
    String email,
    String telefone,
    boolean ativo,
    LocalDateTime criadoAt,
    LocalDateTime atualizadoAt
) {
}
