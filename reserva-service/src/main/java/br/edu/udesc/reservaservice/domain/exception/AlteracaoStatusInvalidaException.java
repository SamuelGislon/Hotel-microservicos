package br.edu.udesc.reservaservice.domain.exception;

public class AlteracaoStatusInvalidaException extends RegraDeNegocioException {

    public AlteracaoStatusInvalidaException(String message) {
        super(message);
    }
}
