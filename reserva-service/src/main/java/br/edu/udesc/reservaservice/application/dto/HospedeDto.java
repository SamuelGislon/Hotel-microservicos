package br.edu.udesc.reservaservice.application.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record HospedeDto(
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
