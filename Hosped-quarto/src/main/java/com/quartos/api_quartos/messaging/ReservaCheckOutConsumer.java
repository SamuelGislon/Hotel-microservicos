package com.quartos.api_quartos.messaging;

import com.quartos.api_quartos.Service.QuartoService;
import com.quartos.api_quartos.config.RabbitMqConfig;
import com.quartos.api_quartos.dto.ReservaCheckInEvent;
import com.quartos.api_quartos.model.Quarto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReservaCheckOutConsumer {

    private static final String EVENTO_CHECKOUT_REALIZADO = "CHECKOUT_REALIZADO";

    private final QuartoService quartoService;

    @RabbitListener(queues = RabbitMqConfig.FILA_RESERVA_CHECKOUT)
    public void consumir(ReservaCheckInEvent evento) {
        if (evento == null) {
            log.error("Mensagem de check-out recebida sem payload");
            return;
        }

        log.info(
                "Evento de check-out recebido. tipo={}, reservaId={}, quartoServicoId={}",
                evento.eventType(),
                evento.reservaId(),
                evento.quartoServicoId()
        );

        if (!EVENTO_CHECKOUT_REALIZADO.equals(evento.eventType())) {
            log.warn(
                    "Evento ignorado pela fila de check-out. tipo={}, reservaId={}",
                    evento.eventType(),
                    evento.reservaId()
            );
            return;
        }

        if (evento.quartoServicoId() == null) {
            log.error(
                    "Evento de check-out sem quartoServicoId. reservaId={}, quartoIdLegado={}",
                    evento.reservaId(),
                    evento.quartoId()
            );
            return;
        }

        try {
            Quarto quarto = quartoService.marcarComoAguardandoLimpezaPorCheckOut(evento.quartoServicoId());
            log.info(
                    "Check-out processado. reservaId={}, quartoServicoId={}, numeroQuarto={}, status={}",
                    evento.reservaId(),
                    quarto.getId(),
                    quarto.getNumeroQuarto(),
                    quarto.getStatus()
            );
        } catch (RuntimeException exception) {
            log.error(
                    "Falha ao processar check-out. reservaId={}, quartoServicoId={}",
                    evento.reservaId(),
                    evento.quartoServicoId(),
                    exception
            );
        }
    }
}
