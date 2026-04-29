package br.edu.udesc.reservaservice.infrastructure.integration.gateway;

import java.util.UUID;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.integracao.pagamento.strategy", havingValue = "fake", matchIfMissing = true)
public class FakePagamentoGateway implements PagamentoGateway {

    @Override
    public StatusPagamentoIntegracao consultarStatusReserva(UUID reservaId) {
        return new StatusPagamentoIntegracao(
            reservaId,
            "SEM_PAGAMENTO_REAL",
            false,
            "Gateway fake em uso: não existe integração real com pagamento-service nesta versão"
        );
    }

    @Override
    public void confirmarPagamentoReserva(UUID reservaId) {
        // Gateway fake usado em modo standalone.
    }
}
