package br.edu.udesc.reservaservice.infrastructure.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private Integracao integracao = new Integracao();

    @Getter
    @Setter
    public static class Integracao {
        private Quarto quarto = new Quarto();
        private Pagamento pagamento = new Pagamento();
    }

    @Getter
    @Setter
    public static class Quarto {
        private String strategy = "fake";
        private String baseUrl = "http://localhost:8080";
        private boolean permitirReservaSemValidacaoExterna = true;
    }

    @Getter
    @Setter
    public static class Pagamento {
        private String strategy = "fake";
        private String baseUrl = "http://localhost:8080";
    }
}
