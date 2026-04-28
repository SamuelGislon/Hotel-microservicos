package br.edu.udesc.reservaservice.infrastructure.messaging.consumer;

import static org.mockito.Mockito.verify;

import br.edu.udesc.reservaservice.application.service.ReservaService;
import br.edu.udesc.reservaservice.infrastructure.messaging.payload.PagamentoProcessadoPayload;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PagamentoProcessadoConsumerTest {

    @Mock
    private ReservaService reservaService;

    @Test
    void deveConfirmarPagamentoAprovado() {
        PagamentoProcessadoConsumer consumer = new PagamentoProcessadoConsumer(reservaService);
        UUID reservaId = UUID.randomUUID();

        consumer.consumir(new PagamentoProcessadoPayload(reservaId.toString(), "APROVADO", LocalDateTime.now()));

        verify(reservaService).confirmarPagamentoPorEventoExterno(reservaId);
    }

    @Test
    void deveCancelarReservaQuandoPagamentoExpirar() {
        PagamentoProcessadoConsumer consumer = new PagamentoProcessadoConsumer(reservaService);
        UUID reservaId = UUID.randomUUID();

        consumer.consumir(new PagamentoProcessadoPayload(reservaId.toString(), "EXPIRADO", LocalDateTime.now()));

        verify(reservaService).cancelarPorPagamentoExpirado(reservaId);
    }
}
