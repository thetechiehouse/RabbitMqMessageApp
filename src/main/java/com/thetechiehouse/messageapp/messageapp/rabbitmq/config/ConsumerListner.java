package com.thetechiehouse.messageapp.messageapp.rabbitmq.config;

import java.io.IOException;

import javax.annotation.PostConstruct;

import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.ChannelAwareMessageListener;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;

//public class Consumer implements MessageListener {

@Component
public class ConsumerListner implements ChannelAwareMessageListener {

    @PostConstruct
    public void init() {
        System.out.println("======ConsumerListner===");
    }

    private void logAndThrowRejectException(Message message) {
        System.out.println("Some  problem occured in processing message.. :" + message.getBody());
        throw new AmqpRejectAndDontRequeueException("Some  problem occured in processing message...");
    }

    public void onMessage(Message message, Channel channel) throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        JsonNode jsonNode = null;
        try {
            jsonNode = mapper.readTree(message.getBody());
        } catch (JsonProcessingException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        int id = jsonNode.get("id").asInt();
        if (id % 2 == 0) {
            logAndThrowRejectException(message);
        }

        System.out.println("I  am in consumer" + new String(message.getBody()));
        try {
            Thread.currentThread().sleep(6000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

}
