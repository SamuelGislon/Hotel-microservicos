package br.edu.udesc.reservaservice.infrastructure.integration.client;

public record QuartoServicoResponse(
    Long id,
    Integer numeroQuarto,
    Integer capacidade,
    String tipo,
    String status
) {
}
