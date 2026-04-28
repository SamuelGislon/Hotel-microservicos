package br.edu.udesc.reservaservice.infrastructure.messaging.producer;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import br.edu.udesc.reservaservice.application.service.EventoPublicacaoLogService;
import br.edu.udesc.reservaservice.domain.event.ReservaDomainEvent;
import br.edu.udesc.reservaservice.infrastructure.config.RabbitMqConfig;
import br.edu.udesc.reservaservice.infrastructure.messaging.payload.ReservaEventoPayload;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

@ExtendWith(MockitoExtension.class)
class ReservaEventoRabbitProducerTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Mock
    private EventoPublicacaoLogService eventoPublicacaoLogService;

    @Test
    void devePublicarEventoNoRabbitMq() {
        ReservaEventoRabbitProducer producer = new ReservaEventoRabbitProducer(
            rabbitTemplate,
            new ObjectMapper().registerModule(new JavaTimeModule()),
            eventoPublicacaoLogService
        );

        ReservaDomainEvent event = new ReservaDomainEvent(
            UUID.randomUUID(),
            "RESERVA_CRIADA",
            LocalDateTime.now(),
            UUID.randomUUID(),
            UUID.randomUUID(),
            UUID.randomUUID(),
            br.edu.udesc.reservaservice.domain.enums.ReservaStatus.PENDENTE,
            br.edu.udesc.reservaservice.domain.enums.PagamentoModo.PAGO_NO_HOTEL,
            br.edu.udesc.reservaservice.domain.enums.PagamentoStatus.NAO_APLICAVEL
        );

        producer.publicarEvento(event);

        verify(rabbitTemplate).convertAndSend(
            eq(RabbitMqConfig.EXCHANGE_RESERVA_EVENTOS),
            eq(RabbitMqConfig.ROUTING_KEY_RESERVA_CRIADA),
            any(ReservaEventoPayload.class)
        );
        verify(eventoPublicacaoLogService).registrarSucesso(eq(event), any());
    }

    @Test
    void deveRegistrarFalhaQuandoPublicacaoFalhar() {
        ReservaEventoRabbitProducer producer = new ReservaEventoRabbitProducer(
            rabbitTemplate,
            new ObjectMapper().registerModule(new JavaTimeModule()),
            eventoPublicacaoLogService
        );

        ReservaDomainEvent event = new ReservaDomainEvent(
            UUID.randomUUID(),
            "RESERVA_CRIADA",
            LocalDateTime.now(),
            UUID.randomUUID(),
            UUID.randomUUID(),
            UUID.randomUUID(),
            br.edu.udesc.reservaservice.domain.enums.ReservaStatus.PENDENTE,
            br.edu.udesc.reservaservice.domain.enums.PagamentoModo.PAGO_NO_HOTEL,
            br.edu.udesc.reservaservice.domain.enums.PagamentoStatus.NAO_APLICAVEL
        );

        doThrow(new RuntimeException("falha no broker")).when(rabbitTemplate).convertAndSend(
            any(String.class),
            any(String.class),
            any(ReservaEventoPayload.class)
        );

        producer.publicarEvento(event);

        verify(eventoPublicacaoLogService).registrarFalha(eq(event), any(), any());
    }
}
