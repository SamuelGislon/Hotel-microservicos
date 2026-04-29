package br.edu.udesc.reservaservice.infrastructure.messaging.producer;

import br.edu.udesc.reservaservice.domain.event.PagamentoReservaCriadaEvent;
import br.edu.udesc.reservaservice.domain.event.PagamentoReservaCriadaEventPublisher;
import br.edu.udesc.reservaservice.domain.exception.IntegracaoExternaException;
import br.edu.udesc.reservaservice.infrastructure.config.RabbitMqConfig;
import br.edu.udesc.reservaservice.infrastructure.messaging.payload.PagamentoReservaCriadaPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SpringPagamentoReservaCriadaEventPublisher implements PagamentoReservaCriadaEventPublisher {

    private static final int MAX_TENTATIVAS = 3;

    private final RabbitTemplate rabbitTemplate;

    @Override
    public void publicar(PagamentoReservaCriadaEvent event) {
        PagamentoReservaCriadaPayload payload = new PagamentoReservaCriadaPayload(
            event.reservaId().toString(),
            event.nomeHospede(),
            event.emailHospede(),
            event.valorDiaria(),
            event.checkIn(),
            event.checkOut(),
            event.metodoPagamento()
        );

        for (int tentativa = 1; tentativa <= MAX_TENTATIVAS; tentativa++) {
            try {
                rabbitTemplate.convertAndSend(
                    RabbitMqConfig.EXCHANGE_HOSPED_EVENTOS,
                    RabbitMqConfig.ROUTING_KEY_HOSPED_RESERVA_CRIADA,
                    payload
                );
                log.info("Evento de reserva criada enviado para pagamento. reservaId={}", event.reservaId());
                return;
            } catch (RuntimeException exception) {
                log.warn("Falha ao publicar evento de pagamento. reservaId={}, tentativa={}", event.reservaId(), tentativa, exception);
            }
        }

        throw new IntegracaoExternaException("Falha ao publicar evento para criação de pagamento");
    }
}
