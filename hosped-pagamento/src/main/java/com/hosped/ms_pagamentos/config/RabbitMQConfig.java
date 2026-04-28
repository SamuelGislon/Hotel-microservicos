package com.hosped.ms_pagamentos.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String FILA_RESERVAS_CRIADAS = "ms-pagamentos.reservas-criadas";
    public static final String FILA_RETORNO_PAGAMENTOS = "ms-reservas.pagamentos-processados";
    public static final String EXCHANGE = "hosped.exchange";
    public static final String RK_RESERVA_CRIADA = "reserva.criada";
    public static final String RK_PAGAMENTO_PROCESSADO = "pagamento.processado";

    @Autowired
    private ConnectionFactory connectionFactory;

    @Bean
    public TopicExchange hospedeExchange() {
        return new TopicExchange(EXCHANGE, true, false);
    }

    @Bean
    public Queue filaReservasCriadas() {
        return QueueBuilder.durable(FILA_RESERVAS_CRIADAS).build();
    }

    @Bean
    public Queue filaRetornoPagamentos() {
        return QueueBuilder.durable(FILA_RETORNO_PAGAMENTOS).build();
    }

    @Bean
    public Binding bindingReservasCriadas(Queue filaReservasCriadas, TopicExchange hospedeExchange) {
        return BindingBuilder.bind(filaReservasCriadas).to(hospedeExchange).with(RK_RESERVA_CRIADA);
    }

    @Bean
    public Binding bindingRetornoPagamentos(Queue filaRetornoPagamentos, TopicExchange hospedeExchange) {
        return BindingBuilder.bind(filaRetornoPagamentos).to(hospedeExchange).with(RK_PAGAMENTO_PROCESSADO);
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate() {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }
}