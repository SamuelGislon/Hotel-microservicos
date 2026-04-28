package br.edu.udesc.reservaservice.application.dto;

public record AtualizarHospedeCommand(
    String nomeCompleto,
    String cpf,
    String email,
    String telefone,
    Boolean ativo
) {
}
