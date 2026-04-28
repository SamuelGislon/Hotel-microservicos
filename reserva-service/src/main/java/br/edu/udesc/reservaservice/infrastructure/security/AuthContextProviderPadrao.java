package br.edu.udesc.reservaservice.infrastructure.security;

import org.springframework.stereotype.Component;

@Component
public class AuthContextProviderPadrao implements AuthContextProvider {

    @Override
    public String identificarSolicitanteAtual() {
        return "sistema-local";
    }
}
