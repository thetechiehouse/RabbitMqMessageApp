package com.thetechiehouse.messageapp.messageapp.rabbitmq.config;

import java.io.IOException;

import javax.annotation.PostConstruct;

import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

//public class Consumer implements MessageListener {

@Component
public class Consumer {

    @PostConstruct
    public void init() {
        System.out.println("==========");
    }

    // public void onMessage(Message message) {

    @RabbitListener(queues = { "simple.queue.name" })
    public void handleMessage(Message message) {
        System.out.println("43232424234");
        logAndThrowRejectException(message);
    }

    private void logAndThrowRejectException(Message message) {
        System.out.println("Email processing failed :" + message.getBody());
        throw new AmqpRejectAndDontRequeueException("Email processing unsuccessful pushing the message to retry queue");
    }

}
