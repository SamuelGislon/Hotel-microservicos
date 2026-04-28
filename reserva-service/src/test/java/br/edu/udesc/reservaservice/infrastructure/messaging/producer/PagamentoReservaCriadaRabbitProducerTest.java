package br.edu.udesc.reservaservice.infrastructure.messaging.producer;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import br.edu.udesc.reservaservice.domain.enums.MetodoPagamento;
import br.edu.udesc.reservaservice.domain.event.PagamentoReservaCriadaEvent;
import br.edu.udesc.reservaservice.infrastructure.config.RabbitMqConfig;
import br.edu.udesc.reservaservice.infrastructure.messaging.payload.PagamentoReservaCriadaPayload;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

@ExtendWith(MockitoExtension.class)
class PagamentoReservaCriadaRabbitProducerTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Test
    void devePublicarEventoNoContratoDoPagamento() {
        PagamentoReservaCriadaRabbitProducer producer = new PagamentoReservaCriadaRabbitProducer(rabbitTemplate);

        producer.publicarEvento(criarEvento());

        verify(rabbitTemplate).convertAndSend(
            eq(RabbitMqConfig.EXCHANGE_HOSPED_EVENTOS),
            eq(RabbitMqConfig.ROUTING_KEY_HOSPED_RESERVA_CRIADA),
            any(PagamentoReservaCriadaPayload.class)
        );
    }

    @Test
    void deveTolerarFalhaAoPublicarEventoNoRabbitMq() {
        PagamentoReservaCriadaRabbitProducer producer = new PagamentoReservaCriadaRabbitProducer(rabbitTemplate);
        doThrow(new RuntimeException("broker indisponível")).when(rabbitTemplate).convertAndSend(
            any(String.class),
            any(String.class),
            any(PagamentoReservaCriadaPayload.class)
        );

        producer.publicarEvento(criarEvento());

        verify(rabbitTemplate).convertAndSend(
            eq(RabbitMqConfig.EXCHANGE_HOSPED_EVENTOS),
            eq(RabbitMqConfig.ROUTING_KEY_HOSPED_RESERVA_CRIADA),
            any(PagamentoReservaCriadaPayload.class)
        );
    }

    private PagamentoReservaCriadaEvent criarEvento() {
        return new PagamentoReservaCriadaEvent(
            UUID.randomUUID(),
            "Maria Silva",
            "maria@email.com",
            BigDecimal.valueOf(250),
            LocalDate.now().plusDays(1),
            LocalDate.now().plusDays(2),
            MetodoPagamento.PIX
        );
    }
}
