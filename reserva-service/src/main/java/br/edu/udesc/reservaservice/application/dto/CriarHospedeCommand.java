package br.edu.udesc.reservaservice.application.dto;

public record CriarHospedeCommand(
    String nomeCompleto,
    String cpf,
    String email,
    String telefone
) {
}
