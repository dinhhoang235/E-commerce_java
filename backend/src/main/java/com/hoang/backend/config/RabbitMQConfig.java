package com.hoang.backend.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableRabbit
public class RabbitMQConfig {

    static final String ORDER_EXCHANGE = "order.exchange";
    static final String PAYMENT_EXCHANGE = "payment.exchange";

    static final String ORDER_CREATED_QUEUE = "order.created.queue";
    static final String ORDER_STATUS_CHANGED_QUEUE = "order.status.changed.queue";
    static final String PAYMENT_SUCCEEDED_QUEUE = "payment.succeeded.queue";

    static final String ORDER_CREATED_KEY = "order.created";
    static final String ORDER_STATUS_CHANGED_KEY = "order.status.changed";
    static final String PAYMENT_SUCCEEDED_KEY = "payment.succeeded";

    @SuppressWarnings("removal")
    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public TopicExchange orderExchange() {
        return new TopicExchange(ORDER_EXCHANGE);
    }

    @Bean
    public TopicExchange paymentExchange() {
        return new TopicExchange(PAYMENT_EXCHANGE);
    }

    @Bean
    public Queue orderCreatedQueue() {
        return new Queue(ORDER_CREATED_QUEUE, true);
    }

    @Bean
    public Queue orderStatusChangedQueue() {
        return new Queue(ORDER_STATUS_CHANGED_QUEUE, true);
    }

    @Bean
    public Queue paymentSucceededQueue() {
        return new Queue(PAYMENT_SUCCEEDED_QUEUE, true);
    }

    @Bean
    public Binding orderCreatedBinding() {
        return BindingBuilder.bind(orderCreatedQueue())
                .to(orderExchange())
                .with(ORDER_CREATED_KEY);
    }

    @Bean
    public Binding orderStatusChangedBinding() {
        return BindingBuilder.bind(orderStatusChangedQueue())
                .to(orderExchange())
                .with(ORDER_STATUS_CHANGED_KEY);
    }

    @Bean
    public Binding paymentSucceededBinding() {
        return BindingBuilder.bind(paymentSucceededQueue())
                .to(paymentExchange())
                .with(PAYMENT_SUCCEEDED_KEY);
    }
}
