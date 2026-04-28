package br.edu.udesc.reservaservice.infrastructure.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.Jackson2JavaTypeMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableRabbit
public class RabbitMqConfig {

    public static final String EXCHANGE_RESERVA_EVENTOS = "reserva.eventos.exchange";
    public static final String EXCHANGE_INTEGRACAO_EVENTOS = "integracao.eventos.exchange";
    public static final String EXCHANGE_HOSPED_EVENTOS = "hosped.exchange";

    public static final String FILA_RESERVA_MONITORAMENTO = "reserva.eventos.monitoramento.queue";
    public static final String FILA_PAGAMENTO_CONFIRMADO = "pagamento.reserva.confirmado.queue";
    public static final String FILA_HOSPED_PAGAMENTOS_PROCESSADOS = "ms-reservas.pagamentos-processados";

    public static final String ROUTING_KEY_RESERVA_CRIADA = "reserva.criada";
    public static final String ROUTING_KEY_RESERVA_CHECKIN = "reserva.checkin.realizado";
    public static final String ROUTING_KEY_RESERVA_CHECKOUT = "reserva.checkout.realizado";
    public static final String ROUTING_KEY_RESERVA_PAGAMENTO_CONFIRMADO = "reserva.pagamento.confirmado";
    public static final String ROUTING_KEY_PAGAMENTO_CONFIRMADO_EXTERNO = "pagamento.reserva.confirmado";
    public static final String ROUTING_KEY_HOSPED_RESERVA_CRIADA = "reserva.criada";
    public static final String ROUTING_KEY_HOSPED_PAGAMENTO_PROCESSADO = "pagamento.processado";

    @Bean
    public TopicExchange reservaEventosExchange() {
        return new TopicExchange(EXCHANGE_RESERVA_EVENTOS, true, false);
    }

    @Bean
    public TopicExchange integracaoEventosExchange() {
        return new TopicExchange(EXCHANGE_INTEGRACAO_EVENTOS, true, false);
    }

    @Bean
    public TopicExchange hospedEventosExchange() {
        return new TopicExchange(EXCHANGE_HOSPED_EVENTOS, true, false);
    }

    @Bean
    public Queue filaReservaMonitoramento() {
        return new Queue(FILA_RESERVA_MONITORAMENTO, true);
    }

    @Bean
    public Queue filaPagamentoConfirmado() {
        return new Queue(FILA_PAGAMENTO_CONFIRMADO, true);
    }

    @Bean
    public Queue filaHospedPagamentosProcessados() {
        return new Queue(FILA_HOSPED_PAGAMENTOS_PROCESSADOS, true);
    }

    @Bean
    public Binding bindingReservaMonitoramento() {
        return BindingBuilder.bind(filaReservaMonitoramento())
            .to(reservaEventosExchange())
            .with("reserva.#");
    }

    @Bean
    public Binding bindingPagamentoConfirmado() {
        return BindingBuilder.bind(filaPagamentoConfirmado())
            .to(integracaoEventosExchange())
            .with(ROUTING_KEY_PAGAMENTO_CONFIRMADO_EXTERNO);
    }

    @Bean
    public Binding bindingHospedPagamentosProcessados() {
        return BindingBuilder.bind(filaHospedPagamentosProcessados())
            .to(hospedEventosExchange())
            .with(ROUTING_KEY_HOSPED_PAGAMENTO_PROCESSADO);
    }

    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter(ObjectMapper objectMapper) {
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter(objectMapper);
        converter.setTypePrecedence(Jackson2JavaTypeMapper.TypePrecedence.INFERRED);
        return converter;
    }
}
