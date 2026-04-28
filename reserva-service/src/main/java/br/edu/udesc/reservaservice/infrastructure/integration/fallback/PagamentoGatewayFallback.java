package br.edu.udesc.reservaservice.infrastructure.integration.fallback;

import br.edu.udesc.reservaservice.infrastructure.integration.gateway.StatusPagamentoIntegracao;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class PagamentoGatewayFallback {

    public StatusPagamentoIntegracao aplicar(UUID reservaId, Throwable throwable) {
        return new StatusPagamentoIntegracao(
            reservaId,
            "FALLBACK",
            true,
            "Fallback acionado: pagamento-service indisponível no momento"
        );
    }
}
