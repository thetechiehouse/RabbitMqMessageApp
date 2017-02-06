package com.thetechiehouse.messageapp.messageapp.rabbitmq.config;

import java.io.Serializable;

public class CustomMessage implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private int id;
    private String name;

    
    public CustomMessage() {
        super();
        // TODO Auto-generated constructor stub
    }

    public CustomMessage(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "CustomMessage{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}
