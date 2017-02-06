package com.thetechiehouse.messageapp.messageapp.rabbitmq.config;

import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class Producer {

    public static void main(String[] args) {                   
        ApplicationContext ctx = new AnnotationConfigApplicationContext(RabbitMqConfig.class, Consumer.class);
        RabbitTemplate rabbitTemplate = ctx.getBean(RabbitTemplate.class);

        AtomicInteger counter = new AtomicInteger();
        for (int i = 0; i < 5; i++){
            System.out.println("sending new custom message..");
            rabbitTemplate.convertAndSend(new CustomMessage(counter.incrementAndGet(), "RabbitMQ Spring JSON Example"));
        }
    }

}
