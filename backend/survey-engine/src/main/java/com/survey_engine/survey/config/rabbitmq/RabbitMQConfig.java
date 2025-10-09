package com.survey.survey.config.rabbitmq;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configures the RabbitMQ message broker topology for the survey application.
 * This setup includes exchanges, queues, and bindings for asynchronous response processing
 * and a dead-letter queue for resilient error handling.
 */
@Configuration
public class RabbitMQConfig {

    public static final String SURVEY_EXCHANGE = "survey.exchange";
    public static final String RESPONSE_QUEUE = "survey.responses.queue";
    public static final String RESPONSE_ROUTING_KEY = "survey.response.submitted";

    public static final String DEAD_LETTER_EXCHANGE = "survey.dlx";
    public static final String DEAD_LETTER_QUEUE = "survey.responses.dlq";

    /**
     * Defines the main topic exchange for survey-related events.
     * @return A TopicExchange instance.
     */
    @Bean
    public TopicExchange surveyExchange() {
        return new TopicExchange(SURVEY_EXCHANGE);
    }

    /**
     * Defines the queue that holds survey responses for processing.
     * It is configured to route failed messages to the dead-letter exchange.
     * @return A durable Queue instance.
     */
    @Bean
    public Queue responseQueue() {
        return QueueBuilder.durable(RESPONSE_QUEUE)
                .withArgument("x-dead-letter-exchange", DEAD_LETTER_EXCHANGE)
                .build();
    }

    /**
     * Binds the response queue to the survey exchange with a specific routing key.
     * @param surveyExchange The main topic exchange.
     * @param responseQueue The queue for survey responses.
     * @return A Binding instance.
     */
    @Bean
    public Binding responseBinding(TopicExchange surveyExchange, Queue responseQueue) {
        return BindingBuilder.bind(responseQueue).to(surveyExchange).with(RESPONSE_ROUTING_KEY);
    }

    /**
     * Defines the dead-letter exchange where failed messages are sent.
     * @return A DirectExchange instance.
     */
    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(DEAD_LETTER_EXCHANGE);
    }

    /**
     * Defines the dead-letter queue that stores failed messages for inspection.
     * @return A durable Queue instance.
     */
    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable(DEAD_LETTER_QUEUE).build();
    }

    /**
     * Binds the dead-letter queue to the dead-letter exchange.
     * The routing key is the name of the original queue.
     * @param deadLetterExchange The dead-letter exchange.
     * @param deadLetterQueue The dead-letter queue.
     * @return A Binding instance.
     */
    @Bean
    public Binding deadLetterBinding(DirectExchange deadLetterExchange, Queue deadLetterQueue) {
        return BindingBuilder.bind(deadLetterQueue).to(deadLetterExchange).with(RESPONSE_QUEUE);
    }

    /**
     * Defines the message converter to serialize and deserialize messages to/from JSON.
     * @return A Jackson2JsonMessageConverter instance.
     */
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * Configures the RabbitTemplate to use the JSON message converter.
     * @param connectionFactory The RabbitMQ connection factory.
     * @return A configured RabbitTemplate instance.
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        final RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        return rabbitTemplate;
    }
}
