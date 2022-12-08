package org.example;


import com.google.gson.Gson;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.sun.org.slf4j.internal.Logger;
import com.sun.org.slf4j.internal.LoggerFactory;
import org.example.Model.Customer;
import org.example.Model.CustomerRequest;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

public class ApiSender {
    Logger logger = LoggerFactory.getLogger(ApiSender.class);
    private static final String EXCHANGE_NAME = "exchange_info";
    private static final String QUEUE_NAME = "queue_info";

    private static final SecureRandom secureRandom = new SecureRandom(); //threadsafe
    private static final Base64.Encoder base64Encoder = Base64.getUrlEncoder(); //threadsafe


    public void sendToCore(String data){

        String message = createRequest(data);
//        logger.info("");
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");

        try(Connection connection = factory.newConnection();
            Channel channel = connection.createChannel()){

            // declare exchange
            channel.exchangeDeclare(EXCHANGE_NAME, "direct");

            // declare properties of request message
            final String corrId = UUID.randomUUID().toString();
            String replyQueueName = channel.queueDeclare().getQueue();

            AMQP.BasicProperties props = new AMQP.BasicProperties
                    .Builder()
                    .correlationId(corrId)
                    .replyTo(replyQueueName)
                    .build();

            // publish messgage
            channel.basicPublish(EXCHANGE_NAME, QUEUE_NAME, props, message.getBytes("UTF-8"));


//            channel.exchangeDeclare(EXCHANGE_NAME, "");
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (TimeoutException e) {
            throw new RuntimeException(e);
        }
    }

    public String createRequest(String data){
        String token = generateNewToken();
        CustomerRequest customerRquest = new CustomerRequest(token, data);
        String json = new Gson().toJson(customerRquest);
        return json;
    }

    public static String generateNewToken() {
        byte[] randomBytes = new byte[24];
        secureRandom.nextBytes(randomBytes);
        return base64Encoder.encodeToString(randomBytes);
    }
}
