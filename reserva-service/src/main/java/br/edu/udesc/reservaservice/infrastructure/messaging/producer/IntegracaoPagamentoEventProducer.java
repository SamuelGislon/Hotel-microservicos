package br.edu.udesc.reservaservice.infrastructure.messaging.producer;

import br.edu.udesc.reservaservice.infrastructure.config.RabbitMqConfig;
import br.edu.udesc.reservaservice.infrastructure.messaging.payload.PagamentoReservaConfirmadoExternoPayload;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class IntegracaoPagamentoEventProducer {

    private final RabbitTemplate rabbitTemplate;

    public void publicarConfirmacaoPagamento(UUID reservaId) {
        PagamentoReservaConfirmadoExternoPayload payload = new PagamentoReservaConfirmadoExternoPayload(
            UUID.randomUUID(),
            "PagamentoReservaConfirmadoExterno",
            LocalDateTime.now(),
            reservaId
        );
        rabbitTemplate.convertAndSend(
            RabbitMqConfig.EXCHANGE_INTEGRACAO_EVENTOS,
            RabbitMqConfig.ROUTING_KEY_PAGAMENTO_CONFIRMADO_EXTERNO,
            payload
        );
    }
}
