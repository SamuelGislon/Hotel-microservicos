package br.edu.udesc.reservaservice.domain.event;

public interface PagamentoReservaCriadaEventPublisher {

    void publicar(PagamentoReservaCriadaEvent event);
}
