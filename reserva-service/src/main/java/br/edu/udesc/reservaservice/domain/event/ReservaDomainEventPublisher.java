package br.edu.udesc.reservaservice.domain.event;

public interface ReservaDomainEventPublisher {

    void publicar(ReservaDomainEvent event);
}
