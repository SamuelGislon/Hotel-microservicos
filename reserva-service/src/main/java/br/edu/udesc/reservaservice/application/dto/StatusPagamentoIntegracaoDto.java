package br.edu.udesc.reservaservice.application.dto;

import java.util.UUID;

public record StatusPagamentoIntegracaoDto(
    UUID reservaId,
    String statusExterno,
    boolean fallbackAcionado,
    String mensagem
) {
}
