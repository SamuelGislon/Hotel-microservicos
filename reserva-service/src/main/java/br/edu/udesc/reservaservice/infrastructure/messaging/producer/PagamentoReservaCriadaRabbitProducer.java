package br.edu.udesc.reservaservice.infrastructure.messaging.producer;

import br.edu.udesc.reservaservice.domain.event.PagamentoReservaCriadaEvent;
import br.edu.udesc.reservaservice.infrastructure.config.RabbitMqConfig;
import br.edu.udesc.reservaservice.infrastructure.messaging.payload.PagamentoReservaCriadaPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class PagamentoReservaCriadaRabbitProducer {

    private final RabbitTemplate rabbitTemplate;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void publicarEvento(PagamentoReservaCriadaEvent event) {
        PagamentoReservaCriadaPayload payload = new PagamentoReservaCriadaPayload(
            event.reservaId().toString(),
            event.nomeHospede(),
            event.emailHospede(),
            event.valorDiaria(),
            event.checkIn(),
            event.checkOut(),
            event.metodoPagamento()
        );

        try {
            rabbitTemplate.convertAndSend(
                RabbitMqConfig.EXCHANGE_HOSPED_EVENTOS,
                RabbitMqConfig.ROUTING_KEY_HOSPED_RESERVA_CRIADA,
                payload
            );
            log.info("Evento de reserva criada enviado para pagamento. reservaId={}", event.reservaId());
        } catch (Exception exception) {
            log.warn("Falha ao enviar evento de reserva criada para pagamento. reservaId={}", event.reservaId(), exception);
        }
    }
}
