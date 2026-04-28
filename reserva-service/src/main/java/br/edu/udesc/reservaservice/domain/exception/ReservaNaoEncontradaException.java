package br.edu.udesc.reservaservice.domain.exception;

import java.util.UUID;

public class ReservaNaoEncontradaException extends RuntimeException {

    public ReservaNaoEncontradaException(UUID reservaId) {
        super("Reserva não encontrada para o id " + reservaId);
    }
}
