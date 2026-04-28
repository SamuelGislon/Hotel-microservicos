package br.edu.udesc.reservaservice.application.dto;

import java.util.UUID;

public record DisponibilidadeQuartoDto(
    UUID quartoId,
    boolean disponivel,
    boolean fallbackAcionado,
    String mensagem
) {
}
