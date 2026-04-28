package br.edu.udesc.reservaservice.infrastructure.messaging.producer;

import br.edu.udesc.reservaservice.application.service.EventoPublicacaoLogService;
import br.edu.udesc.reservaservice.domain.event.ReservaDomainEvent;
import br.edu.udesc.reservaservice.infrastructure.config.RabbitMqConfig;
import br.edu.udesc.reservaservice.infrastructure.messaging.payload.ReservaEventoPayload;
import br.edu.udesc.reservaservice.shared.util.JsonUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReservaEventoRabbitProducer {

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;
    private final EventoPublicacaoLogService eventoPublicacaoLogService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void publicarEvento(ReservaDomainEvent event) {
        ReservaEventoPayload payload = new ReservaEventoPayload(
            event.eventId(),
            event.eventType(),
            event.occurredAt(),
            event.reservaId(),
            event.hospedeId(),
            event.quartoId(),
            event.reservaStatus(),
            event.pagamentoModo(),
            event.pagamentoStatus()
        );

        String payloadResumo = JsonUtils.serializarResumido(objectMapper, payload);

        try {
            rabbitTemplate.convertAndSend(
                RabbitMqConfig.EXCHANGE_RESERVA_EVENTOS,
                resolverRoutingKey(event.eventType()),
                payload
            );
            eventoPublicacaoLogService.registrarSucesso(event, payloadResumo);
            log.info("Evento publicado com sucesso. tipo={}, reservaId={}", event.eventType(), event.reservaId());
        } catch (Exception exception) {
            eventoPublicacaoLogService.registrarFalha(event, payloadResumo, exception.getMessage());
            log.error("Falha ao publicar evento {} da reserva {}", event.eventType(), event.reservaId(), exception);
        }
    }

    private String resolverRoutingKey(String eventType) {
        return switch (eventType) {
            case "RESERVA_CRIADA" -> RabbitMqConfig.ROUTING_KEY_RESERVA_CRIADA;
            case "CHECKIN_REALIZADO" -> RabbitMqConfig.ROUTING_KEY_RESERVA_CHECKIN;
            case "CHECKOUT_REALIZADO" -> RabbitMqConfig.ROUTING_KEY_RESERVA_CHECKOUT;
            case "PAGAMENTO_RESERVA_CONFIRMADO" -> RabbitMqConfig.ROUTING_KEY_RESERVA_PAGAMENTO_CONFIRMADO;
            default -> "reserva.desconhecido";
        };
    }
}
