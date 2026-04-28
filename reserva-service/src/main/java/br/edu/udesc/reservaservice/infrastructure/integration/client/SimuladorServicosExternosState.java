package br.edu.udesc.reservaservice.infrastructure.integration.client;

import java.util.concurrent.atomic.AtomicBoolean;
import org.springframework.stereotype.Component;

@Component
public class SimuladorServicosExternosState {

    private final AtomicBoolean quartoIndisponivel = new AtomicBoolean(false);
    private final AtomicBoolean pagamentoIndisponivel = new AtomicBoolean(false);

    public boolean isQuartoIndisponivel() {
        return quartoIndisponivel.get();
    }

    public void setQuartoIndisponivel(boolean ativo) {
        quartoIndisponivel.set(ativo);
    }

    public boolean isPagamentoIndisponivel() {
        return pagamentoIndisponivel.get();
    }

    public void setPagamentoIndisponivel(boolean ativo) {
        pagamentoIndisponivel.set(ativo);
    }
}
