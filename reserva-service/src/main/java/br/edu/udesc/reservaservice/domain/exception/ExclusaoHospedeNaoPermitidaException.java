package br.edu.udesc.reservaservice.domain.exception;

public class ExclusaoHospedeNaoPermitidaException extends RegraDeNegocioException {

    public ExclusaoHospedeNaoPermitidaException() {
        super("Não é permitido excluir hóspede com reservas vinculadas ativas, futuras ou históricas");
    }
}
