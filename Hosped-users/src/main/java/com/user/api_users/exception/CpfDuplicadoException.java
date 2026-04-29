package com.user.api_users.exception;

public class CpfDuplicadoException extends RuntimeException {

    public CpfDuplicadoException(String cpf) {
        super("CPF já cadastrado: " + cpf);
    }
}
