package br.edu.udesc.reservaservice.infrastructure.messaging.producer;

import br.edu.udesc.reservaservice.domain.event.PagamentoReservaCriadaEvent;
import br.edu.udesc.reservaservice.domain.event.PagamentoReservaCriadaEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SpringPagamentoReservaCriadaEventPublisher implements PagamentoReservaCriadaEventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    public void publicar(PagamentoReservaCriadaEvent event) {
        applicationEventPublisher.publishEvent(event);
    }
}
