package br.edu.udesc.reservaservice;

import br.edu.udesc.reservaservice.infrastructure.config.AppProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(AppProperties.class)
public class ReservaServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ReservaServiceApplication.class, args);
    }
}
