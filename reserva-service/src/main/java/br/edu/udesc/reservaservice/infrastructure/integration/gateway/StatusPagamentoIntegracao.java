package br.edu.udesc.reservaservice.infrastructure.integration.gateway;

import java.util.UUID;

public record StatusPagamentoIntegracao(
    UUID reservaId,
    String statusExterno,
    boolean fallbackAcionado,
    String mensagem
) {
}
