package br.edu.udesc.reservaservice.infrastructure.messaging.producer;

import br.edu.udesc.reservaservice.domain.event.ReservaDomainEvent;
import br.edu.udesc.reservaservice.domain.event.ReservaDomainEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SpringReservaDomainEventPublisher implements ReservaDomainEventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    public void publicar(ReservaDomainEvent event) {
        applicationEventPublisher.publishEvent(event);
    }
}
