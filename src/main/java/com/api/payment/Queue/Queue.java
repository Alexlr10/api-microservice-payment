package com.api.payment.Queue;

import com.rabbitmq.client.*;
import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeoutException;

public class Queue {
    public static Channel connect() throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(System.getenv("RABBITMQ_DEFAULT_HOST"));
        String portString = System.getenv("RABBITMQ_DEFAULT_PORT");
        if (portString != null && !portString.isEmpty()) {
            factory.setPort(Integer.parseInt(portString));
        }
        factory.setVirtualHost(System.getenv("RABBITMQ_DEFAULT_VHOST"));
        factory.setUsername(System.getenv("RABBITMQ_DEFAULT_USER"));
        factory.setPassword(System.getenv("RABBITMQ_DEFAULT_PASS"));

        Connection connection = factory.newConnection();
        return connection.createChannel();
    }


    public static void notify(byte[] payload, String exchange, String routingKey, Channel channel) throws IOException {
        AMQP.BasicProperties properties = new AMQP.BasicProperties.Builder()
                .contentType("application/json")
                .deliveryMode(2) // make message persistent
                .build();

        channel.basicPublish(exchange, routingKey, properties, payload);
        System.out.println("Message sent");
    }

    public static void startConsuming(String queue, Channel ch, LinkedBlockingQueue<byte[]> inChannel) throws IOException {
        ch.queueDeclare(queue, true, false, false, null);

        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            byte[] message = delivery.getBody();
            try {
                inChannel.put(message);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        };

        ch.basicConsume(queue, true, deliverCallback, consumerTag -> {});
    }

}