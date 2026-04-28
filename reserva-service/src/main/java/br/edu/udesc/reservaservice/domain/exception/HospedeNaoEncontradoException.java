package br.edu.udesc.reservaservice.domain.exception;

import java.util.UUID;

public class HospedeNaoEncontradoException extends RuntimeException {

    public HospedeNaoEncontradoException(UUID hospedeId) {
        super("Hóspede não encontrado para o id " + hospedeId);
    }

    public HospedeNaoEncontradoException(String cpf) {
        super("Hóspede não encontrado para o CPF " + cpf);
    }
}
