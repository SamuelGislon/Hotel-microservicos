package br.edu.udesc.reservaservice.infrastructure.integration.gateway;

import java.util.UUID;

public interface PagamentoGateway {

    StatusPagamentoIntegracao consultarStatusReserva(UUID reservaId);

    void confirmarPagamentoReserva(UUID reservaId);
}
