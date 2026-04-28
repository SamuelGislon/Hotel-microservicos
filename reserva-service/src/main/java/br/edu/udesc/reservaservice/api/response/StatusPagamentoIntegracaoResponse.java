package br.edu.udesc.reservaservice.api.response;

import java.util.UUID;

public record StatusPagamentoIntegracaoResponse(
    UUID reservaId,
    String statusExterno,
    boolean fallbackAcionado,
    String mensagem
) {
}
