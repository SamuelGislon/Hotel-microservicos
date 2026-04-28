package br.edu.udesc.reservaservice.domain.exception;

public class DataReservaInvalidaException extends RegraDeNegocioException {

    public DataReservaInvalidaException() {
        super("A data de check-out deve ser posterior à data de check-in");
    }
}
