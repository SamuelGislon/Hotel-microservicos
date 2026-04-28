package br.edu.udesc.reservaservice.domain.exception;

public class CpfDuplicadoException extends RegraDeNegocioException {

    public CpfDuplicadoException(String cpf) {
        super("Já existe hóspede cadastrado com o CPF " + cpf);
    }
}
