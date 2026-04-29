package com.quartos.api_quartos;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.quartos.api_quartos.Service.QuartoService;
import com.quartos.api_quartos.dto.ReservaCheckInEvent;
import com.quartos.api_quartos.messaging.ReservaCheckInConsumer;
import com.quartos.api_quartos.model.Quarto;
import com.quartos.api_quartos.model.StatusQuarto;
import com.quartos.api_quartos.model.TipoQuarto;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReservaCheckInConsumerTest {

    @Mock
    private QuartoService quartoService;

    private ReservaCheckInConsumer consumer;

    @BeforeEach
    void setUp() {
        consumer = new ReservaCheckInConsumer(quartoService);
    }

    @Test
    void deveMarcarQuartoComoOcupadoAoConsumirCheckIn() {
        Quarto quarto = new Quarto(101, TipoQuarto.SIMPLES, 2);
        quarto.setId(10L);
        quarto.setStatus(StatusQuarto.OCUPADO);
        ReservaCheckInEvent evento = criarEvento(10L);

        when(quartoService.marcarComoOcupadoPorCheckIn(10L)).thenReturn(Optional.of(quarto));

        consumer.consumir(evento);

        verify(quartoService).marcarComoOcupadoPorCheckIn(10L);
    }

    @Test
    void deveIgnorarEventoSemQuartoServicoId() {
        consumer.consumir(criarEvento(null));

        verify(quartoService, never()).marcarComoOcupadoPorCheckIn(null);
    }

    @Test
    void deveTratarQuartoNaoEncontrado() {
        ReservaCheckInEvent evento = criarEvento(99L);
        when(quartoService.marcarComoOcupadoPorCheckIn(99L)).thenReturn(Optional.empty());

        consumer.consumir(evento);

        verify(quartoService).marcarComoOcupadoPorCheckIn(99L);
    }

    private ReservaCheckInEvent criarEvento(Long quartoServicoId) {
        return new ReservaCheckInEvent(
                UUID.randomUUID(),
                "CHECKIN_REALIZADO",
                LocalDateTime.now(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                quartoServicoId,
                "ATIVA",
                "PAGO_NO_HOTEL",
                "NAO_APLICAVEL"
        );
    }
}
