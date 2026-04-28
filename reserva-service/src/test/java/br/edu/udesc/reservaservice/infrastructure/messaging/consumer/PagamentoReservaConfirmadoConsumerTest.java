package br.edu.udesc.reservaservice.infrastructure.messaging.consumer;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import br.edu.udesc.reservaservice.application.service.ReservaService;
import br.edu.udesc.reservaservice.infrastructure.messaging.payload.PagamentoReservaConfirmadoExternoPayload;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PagamentoReservaConfirmadoConsumerTest {

    @Mock
    private ReservaService reservaService;

    @Test
    void deveConsumirEventoExternoDePagamento() {
        PagamentoReservaConfirmadoConsumer consumer = new PagamentoReservaConfirmadoConsumer(reservaService);
        UUID reservaId = UUID.randomUUID();

        consumer.consumir(new PagamentoReservaConfirmadoExternoPayload(
            UUID.randomUUID(),
            "PagamentoReservaConfirmadoExterno",
            LocalDateTime.now(),
            reservaId
        ));

        verify(reservaService).confirmarPagamentoPorEventoExterno(reservaId);
    }

    @Test
    void deveTolerarFalhaControladaNoProcessamento() {
        PagamentoReservaConfirmadoConsumer consumer = new PagamentoReservaConfirmadoConsumer(reservaService);
        UUID reservaId = UUID.randomUUID();
        doThrow(new RuntimeException("erro controlado")).when(reservaService).confirmarPagamentoPorEventoExterno(reservaId);

        consumer.consumir(new PagamentoReservaConfirmadoExternoPayload(
            UUID.randomUUID(),
            "PagamentoReservaConfirmadoExterno",
            LocalDateTime.now(),
            reservaId
        ));
    }
}
