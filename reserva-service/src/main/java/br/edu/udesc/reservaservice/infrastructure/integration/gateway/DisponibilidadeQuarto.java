package br.edu.udesc.reservaservice.infrastructure.integration.gateway;

import java.util.UUID;

public record DisponibilidadeQuarto(
    UUID quartoId,
    Long quartoServicoId,
    String quartoNumero,
    boolean disponivel,
    boolean fallbackAcionado,
    String mensagem
) {

    public DisponibilidadeQuarto(UUID quartoId, boolean disponivel, boolean fallbackAcionado, String mensagem) {
        this(quartoId, null, null, disponivel, fallbackAcionado, mensagem);
    }

    public DisponibilidadeQuarto(Long quartoServicoId, String quartoNumero, boolean disponivel, boolean fallbackAcionado, String mensagem) {
        this(null, quartoServicoId, quartoNumero, disponivel, fallbackAcionado, mensagem);
    }
}
