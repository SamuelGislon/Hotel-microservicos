package br.edu.udesc.reservaservice.infrastructure.messaging.consumer;

import br.edu.udesc.reservaservice.application.service.ReservaService;
import br.edu.udesc.reservaservice.infrastructure.config.RabbitMqConfig;
import br.edu.udesc.reservaservice.infrastructure.messaging.payload.PagamentoReservaConfirmadoExternoPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PagamentoReservaConfirmadoConsumer {

    private final ReservaService reservaService;

    @RabbitListener(queues = RabbitMqConfig.FILA_PAGAMENTO_CONFIRMADO)
    public void consumir(PagamentoReservaConfirmadoExternoPayload payload) {
        try {
            log.info("Evento externo recebido para confirmação de pagamento. reservaId={}", payload.reservaId());
            reservaService.confirmarPagamentoPorEventoExterno(payload.reservaId());
        } catch (Exception exception) {
            log.warn("Falha controlada ao processar evento externo de pagamento para a reserva {}", payload.reservaId(), exception);
        }
    }
}
