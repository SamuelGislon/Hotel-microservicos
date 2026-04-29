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
public class ReservaCheckInConsumer {

    private static final String EVENTO_CHECKIN_REALIZADO = "CHECKIN_REALIZADO";

    private final QuartoService quartoService;

    @RabbitListener(queues = RabbitMqConfig.FILA_RESERVA_CHECKIN)
    public void consumir(ReservaCheckInEvent evento) {
        if (evento == null) {
            log.error("Mensagem de check-in recebida sem payload");
            return;
        }

        if (!EVENTO_CHECKIN_REALIZADO.equals(evento.eventType())) {
            log.warn(
                    "Evento ignorado pela fila de check-in. tipo={}, reservaId={}",
                    evento.eventType(),
                    evento.reservaId()
            );
            return;
        }

        if (evento.quartoServicoId() == null) {
            log.error(
                    "Evento de check-in sem quartoServicoId. reservaId={}, quartoIdLegado={}",
                    evento.reservaId(),
                    evento.quartoId()
            );
            return;
        }

        quartoService.marcarComoOcupadoPorCheckIn(evento.quartoServicoId())
                .ifPresentOrElse(
                        quarto -> registrarSucesso(evento, quarto),
                        () -> log.error(
                                "Quarto nao encontrado ao processar check-in. reservaId={}, quartoServicoId={}",
                                evento.reservaId(),
                                evento.quartoServicoId()
                        )
                );
    }

    private void registrarSucesso(ReservaCheckInEvent evento, Quarto quarto) {
        log.info(
                "Check-in processado. reservaId={}, quartoServicoId={}, numeroQuarto={}, status={}",
                evento.reservaId(),
                quarto.getId(),
                quarto.getNumeroQuarto(),
                quarto.getStatus()
        );
    }
}
