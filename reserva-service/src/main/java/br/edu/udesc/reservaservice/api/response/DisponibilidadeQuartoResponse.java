package br.edu.udesc.reservaservice.api.response;

import java.util.UUID;

public record DisponibilidadeQuartoResponse(
    UUID quartoId,
    boolean disponivel,
    boolean fallbackAcionado,
    String mensagem
) {
}
