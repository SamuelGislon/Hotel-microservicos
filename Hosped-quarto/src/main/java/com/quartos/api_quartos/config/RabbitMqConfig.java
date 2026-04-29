package com.quartos.api_quartos.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.support.converter.Jackson2JavaTypeMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableRabbit
public class RabbitMqConfig {

    public static final String EXCHANGE_RESERVA_EVENTOS = "reserva.eventos.exchange";
    public static final String FILA_RESERVA_CHECKIN = "quarto.reserva.checkin.queue";
    public static final String ROUTING_KEY_RESERVA_CHECKIN = "reserva.checkin.realizado";
    public static final String FILA_RESERVA_CHECKOUT = "quarto.reserva.checkout.queue";
    public static final String ROUTING_KEY_RESERVA_CHECKOUT = "reserva.checkout.realizado";


    @Bean
    public TopicExchange reservaEventosExchange() {
        return new TopicExchange(EXCHANGE_RESERVA_EVENTOS, true, false);
    }

    @Bean
    public Queue filaReservaCheckIn() {
        return new Queue(FILA_RESERVA_CHECKIN, true);
    }

    @Bean
    public Queue filaReservaCheckOut() {
        return new Queue(FILA_RESERVA_CHECKOUT, true);
    }

    @Bean
    public Binding bindingReservaCheckIn() {
        return BindingBuilder.bind(filaReservaCheckIn())
                .to(reservaEventosExchange())
                .with(ROUTING_KEY_RESERVA_CHECKIN);
    }

    @Bean
    public Binding bindingReservaCheckOut() {
        return BindingBuilder.bind(filaReservaCheckOut())
                .to(reservaEventosExchange())
                .with(ROUTING_KEY_RESERVA_CHECKOUT);
    }

    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter(ObjectMapper objectMapper) {
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter(objectMapper);
        converter.setTypePrecedence(Jackson2JavaTypeMapper.TypePrecedence.INFERRED);
        return converter;
    }
}
