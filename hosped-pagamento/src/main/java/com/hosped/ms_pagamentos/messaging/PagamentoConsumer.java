package com.hosped.ms_pagamentos.messaging;

import com.hosped.ms_pagamentos.config.RabbitMQConfig;
import com.hosped.ms_pagamentos.dto.ReservaEventoDTO;
import com.hosped.ms_pagamentos.service.PagamentoService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PagamentoConsumer {

    private final PagamentoService pagamentoService;

    @RabbitListener(queues = RabbitMQConfig.FILA_RESERVAS_CRIADAS)
    public void receberReservaCriada(ReservaEventoDTO evento) {
        pagamentoService.processarReserva(evento);
    }
}