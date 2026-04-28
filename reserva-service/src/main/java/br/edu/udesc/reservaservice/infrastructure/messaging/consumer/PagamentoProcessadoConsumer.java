package br.edu.udesc.reservaservice.infrastructure.messaging.consumer;

import br.edu.udesc.reservaservice.application.service.ReservaService;
import br.edu.udesc.reservaservice.infrastructure.config.RabbitMqConfig;
import br.edu.udesc.reservaservice.infrastructure.messaging.payload.PagamentoProcessadoPayload;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PagamentoProcessadoConsumer {

    private final ReservaService reservaService;

    @RabbitListener(queues = RabbitMqConfig.FILA_HOSPED_PAGAMENTOS_PROCESSADOS)
    public void consumir(PagamentoProcessadoPayload payload) {
        try {
            log.info("Evento de pagamento processado recebido. reservaId={}, status={}", payload.reservaId(), payload.status());
            UUID reservaId = UUID.fromString(payload.reservaId());
            if ("APROVADO".equalsIgnoreCase(payload.status())) {
                reservaService.confirmarPagamentoPorEventoExterno(reservaId);
            } else if ("EXPIRADO".equalsIgnoreCase(payload.status())) {
                reservaService.cancelarPorPagamentoExpirado(reservaId);
            } else {
                log.info("Status de pagamento ignorado para reserva {}: {}", payload.reservaId(), payload.status());
            }
        } catch (Exception exception) {
            log.warn("Falha controlada ao processar evento de pagamento {}", payload, exception);
        }
    }
}
