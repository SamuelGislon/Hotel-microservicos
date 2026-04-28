package br.edu.udesc.reservaservice.infrastructure.messaging.consumer;

import br.edu.udesc.reservaservice.infrastructure.config.RabbitMqConfig;
import br.edu.udesc.reservaservice.infrastructure.messaging.payload.ReservaEventoPayload;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ReservaEventoMonitoramentoConsumer {

    @RabbitListener(queues = RabbitMqConfig.FILA_RESERVA_MONITORAMENTO)
    public void consumir(ReservaEventoPayload payload) {
        log.info(
            "Evento de reserva observado na fila de monitoramento. tipo={}, reservaId={}, status={}",
            payload.eventType(),
            payload.reservaId(),
            payload.reservaStatus()
        );
    }
}
