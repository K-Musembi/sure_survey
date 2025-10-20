package com.survey_engine.payments.config.rabbitmq;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configures RabbitMQ exchanges, queues, and bindings for publishing payment events.
 */
@Configuration
public class PaymentsRabbitMQConfig {

    // Exchange that will receive all payment-related events
    public static final String PAYMENT_EXCHANGE = "payment.exchange";

    // Routing key for when a payment has succeeded
    public static final String PAYMENT_SUCCEEDED_ROUTING_KEY = "payment.succeeded";

    // Queue for the external 'survey-service' to consume successful payment events
    public static final String SURVEY_PAYMENT_SUCCEEDED_QUEUE = "survey.payment.succeeded.queue";

    // Queue for this service to consume its own successful payment events for logging/auditing
    public static final String PAYMENT_SUCCEEDED_LOG_QUEUE = "payment.succeeded.log.queue";

    /**
     * Defines the topic exchange for all payment events.
     * A topic exchange allows for flexible routing of messages based on routing keys.
     * @return A TopicExchange bean.
     */
    @Bean
    public TopicExchange paymentExchange() {
        return new TopicExchange(PAYMENT_EXCHANGE);
    }

    /**
     * Defines the queue that the 'survey-service' will listen to for successful payment notifications.
     * This queue is declared here so that the producer service (payments) can ensure it exists.
     * @return A durable Queue bean.
     */
    @Bean
    public Queue surveyPaymentSucceededQueue() {
        return new Queue(SURVEY_PAYMENT_SUCCEEDED_QUEUE, true);
    }

    /**
     * Defines the queue for the internal logging listener.
     * @return A durable Queue bean.
     */
    @Bean
    public Queue paymentSucceededLogQueue() {
        return new Queue(PAYMENT_SUCCEEDED_LOG_QUEUE, true);
    }

    /**
     * Binds the survey service's queue to the payment exchange using the specific routing key for
     * successful payments. This tells the exchange to forward messages with this key to this queue.
     * @param surveyPaymentSucceededQueue The queue for the survey service.
     * @param paymentExchange The central payment exchange.
     * @return A Binding bean.
     */
    @Bean
    public Binding surveyBinding(Queue surveyPaymentSucceededQueue, TopicExchange paymentExchange) {
        return BindingBuilder.bind(surveyPaymentSucceededQueue).to(paymentExchange).with(PAYMENT_SUCCEEDED_ROUTING_KEY);
    }

    /**
     * Binds the internal logging queue to the payment exchange.
     * This demonstrates a fan-out pattern where one event can be consumed by multiple queues.
     * @param paymentSucceededLogQueue The queue for the internal listener.
     * @param paymentExchange The central payment exchange.
     * @return A Binding bean.
     */
    @Bean
    public Binding logBinding(Queue paymentSucceededLogQueue, TopicExchange paymentExchange) {
        return BindingBuilder.bind(paymentSucceededLogQueue).to(paymentExchange).with(PAYMENT_SUCCEEDED_ROUTING_KEY);
    }

    /**
     * Defines a message converter that serializes/deserializes objects to/from JSON format.
     * This allows sending and receiving POJOs directly.
     * @return A Jackson2JsonMessageConverter bean.
     */
    @Bean
    public MessageConverter paymentJsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * Configures the RabbitTemplate to use the JSON message converter.
     * This ensures that any object sent via RabbitTemplate is automatically converted to JSON.
     * @param connectionFactory The autoconfigured connection factory.
     * @return A configured RabbitTemplate bean.
     */
    @Bean
    public RabbitTemplate paymentRabbitTemplate(ConnectionFactory connectionFactory) {
        final RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(paymentJsonMessageConverter());
        return rabbitTemplate;
    }
}