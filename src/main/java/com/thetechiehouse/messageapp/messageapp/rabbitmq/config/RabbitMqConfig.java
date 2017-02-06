package com.thetechiehouse.messageapp.messageapp.rabbitmq.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.ChannelAwareMessageListener;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.support.converter.JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

@Configuration
@ComponentScan("com.thetechiehouse.messageapp")
public class RabbitMqConfig {

    private static final String SIMPLE_MESSAGE_QUEUE = "simple.queue.name";

    private static final String SECONADARY_MESSAGE_QUEUE = "secondary.queue.name";

    private static final String DEAD_LETTER_ARGS_KEY = "x-dead-letter-exchange";

    private static final String MESSAGE_TTL_ARGS_KEY = "x-message-ttl";

    private static final String DEAD_LETTER_ROUTING_KEY = "x-dead-letter-routing-key";
    

    @Autowired
    @Qualifier("consumerListner")
    private ChannelAwareMessageListener consumerListner;
    

    @Bean
    public RabbitAdmin rabbitAdmin() {
        RabbitAdmin rabbitAdmin = new RabbitAdmin(connectionFactory());
        return rabbitAdmin;
    }

    @Bean
    public ConnectionFactory connectionFactory() {
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory("localhost");
        connectionFactory.setUsername("guest");
        connectionFactory.setPassword("guest");
        return connectionFactory;
    }

    @Bean
    public Queue simpleQueue(RabbitAdmin rabbitAdmin) {

        Queue queue = new Queue(SIMPLE_MESSAGE_QUEUE, true, false, false, getSimpleMessageConfigurations());
        queue.setAdminsThatShouldDeclare(rabbitAdmin);
        return queue;

    }

    @Bean
    public Queue secondaryQueue(RabbitAdmin rabbitAdmin) {

        Queue queue = new Queue(SECONADARY_MESSAGE_QUEUE, true, false, false, getSimpleMessageRetryConfigurations());
        queue.setAdminsThatShouldDeclare(rabbitAdmin);
        return queue;
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate() {
        RabbitTemplate template = new RabbitTemplate(connectionFactory());
        template.setExchange("simple_exchange");
        template.setRoutingKey(SIMPLE_MESSAGE_QUEUE);
        template.setQueue(SIMPLE_MESSAGE_QUEUE);
        template.setMessageConverter(jsonMessageConverter());
        // template.setConfirmCallback(priceAlertPublishConfirmCallback);
        template.setRetryTemplate(retryTemplate());
        return template;
    }

    @Bean
    public RetryTemplate retryTemplate() {
        FixedBackOffPolicy backOffPolicy = new FixedBackOffPolicy();
        backOffPolicy.setBackOffPeriod(2);

        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy();
        retryPolicy.setMaxAttempts(3);

        RetryTemplate retryTemplate = new RetryTemplate();
        retryTemplate.setRetryPolicy(retryPolicy);
        retryTemplate.setBackOffPolicy(backOffPolicy);

        return retryTemplate;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory simpleMessageRabbitListenerContainerFactory(RabbitAdmin rabbitAdmin) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory());

        rabbitAdmin.initialize();

        factory.setConcurrentConsumers(2);
        factory.setDefaultRequeueRejected(false);
        return factory;
    }

    @Bean
    public DirectExchange simpleMessageExchange() {
        DirectExchange exchange = new DirectExchange("simple_exchange", true, false);
        return exchange;
    }

    @Bean
    public DirectExchange simpleMessageRetryExchange() {
        DirectExchange exchange = new DirectExchange("simple_retry_exchange", true, false);
        return exchange;
    }

    @Bean
    public Binding simpleMessageAppBinding(RabbitAdmin rabbitAdmin, Queue simpleQueue) {
        Binding binding = BindingBuilder.bind(simpleQueue).to(simpleMessageExchange()).withQueueName();
        rabbitAdmin.declareBinding(binding);
        return binding;
    }

    @Bean
    public Binding simpleMessageRetryAppBinding(RabbitAdmin rabbitAdmin, Queue secondaryQueue) {
        Binding binding = BindingBuilder.bind(secondaryQueue).to(simpleMessageRetryExchange()).withQueueName();
        rabbitAdmin.declareBinding(binding);
        return binding;

    }

    // @Bean
    public SimpleMessageListenerContainer listenerContainer(RabbitAdmin rabbitAdmin) {
        SimpleMessageListenerContainer listenerContainer = new SimpleMessageListenerContainer();
        listenerContainer.setConnectionFactory(connectionFactory());
        listenerContainer.setQueues(simpleQueue(rabbitAdmin));
        listenerContainer.setMessageConverter(jsonMessageConverter());
        listenerContainer.setMessageListener(new Consumer());
        listenerContainer.setAcknowledgeMode(AcknowledgeMode.AUTO);
        return listenerContainer;
    }

    private Map<String, Object> getSimpleMessageConfigurations() {
        Map<String, Object> arguments = new HashMap<String, Object>();
        arguments.put(DEAD_LETTER_ARGS_KEY, "simple_retry_exchange");
        arguments.put(DEAD_LETTER_ROUTING_KEY, SECONADARY_MESSAGE_QUEUE);
        arguments.put(MESSAGE_TTL_ARGS_KEY, 60l);
        return arguments;
    }

    private Map<String, Object> getSimpleMessageRetryConfigurations() {
        Map<String, Object> arguments = new HashMap<String, Object>();
        arguments.put(DEAD_LETTER_ARGS_KEY, "simple_exchange");
        arguments.put(DEAD_LETTER_ROUTING_KEY, SIMPLE_MESSAGE_QUEUE);
        arguments.put(MESSAGE_TTL_ARGS_KEY, 60000l);
        return arguments;
    }

}
