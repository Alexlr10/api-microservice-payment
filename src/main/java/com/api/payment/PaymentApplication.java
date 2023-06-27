package com.api.payment;

import com.api.payment.Dtos.OrderDTO;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSerializer;
import com.rabbitmq.client.Channel;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.LinkedBlockingQueue;
import java.nio.charset.StandardCharsets;
import com.api.payment.Queue.Queue;

@SpringBootApplication
public class PaymentApplication {

    public static void main(String[] args) throws Exception {
        var inChannel = new LinkedBlockingQueue<byte[]>();
        var connection = Queue.connect();
        Queue.startConsuming("order_queue", connection, inChannel);

        while (true) {
            byte[] message = inChannel.take();
            String jsonMessage = new String(message);
            processOrder(jsonMessage, connection);
        }
    }

    public static void processOrder(String jsonMessage, Channel ch) throws Exception {
        Gson gson = getGson();
        OrderDTO order = gson.fromJson(jsonMessage, OrderDTO.class);
        order.setStatus("aprovado");
        notifyPaymentProcessed(order, ch);
    }

    public static void notifyPaymentProcessed(OrderDTO order, Channel ch) throws Exception {
        String json = order.toJson();
        Queue.notify(json.getBytes(StandardCharsets.UTF_8), "payment_ex", "", ch);
        System.out.println(json);
    }

    public static Gson getGson() {
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(LocalDateTime.class, (JsonSerializer<LocalDateTime>) (src, typeOfSrc, context) ->
                context.serialize(DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(src)));
        return builder.create();
    }
}
