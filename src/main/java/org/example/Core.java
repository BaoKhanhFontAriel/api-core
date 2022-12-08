package org.example;

import com.google.gson.Gson;
import com.rabbitmq.client.*;
import lombok.extern.slf4j.Slf4j;
import org.example.Model.CustomerRequest;
import org.json.JSONObject;
import redis.clients.jedis.Jedis;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.concurrent.TimeoutException;


@Slf4j
public class Core {
    private static final java.sql.Connection oracleConn = OracleConnect.getInstance().getConn();
    private static final String EXCHANGE_NAME = "exchange_info";
    private static final String QUEUE_NAME = "queue_info";
    private static final String ROUTING_KEY = "info";


    private static final int sleep = 3000;
    public static void main(String[] args) throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");

        Connection connection = factory.newConnection();

        // set up channel
        Channel channel = connection.createChannel();
        channel.exchangeDeclare(EXCHANGE_NAME, "direct");
        channel.queueDeclare(QUEUE_NAME, false, false, false, null);
        channel.queuePurge(QUEUE_NAME);
        channel.queueBind(QUEUE_NAME, EXCHANGE_NAME, ROUTING_KEY);

        channel.basicQos(1);

        // do when server receive request
        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            try {
                String json = new String(delivery.getBody(), "UTF-8");
                CustomerRequest customerRquest = new Gson().fromJson(json, CustomerRequest.class);

                pushToRedis(customerRquest);

                AMQP.BasicProperties replyProps = new AMQP.BasicProperties
                        .Builder()
                        .correlationId(delivery.getProperties().getCorrelationId())
                        .build();




            } catch (SQLException e) {
                throw new RuntimeException(e);
            }


//
//            try(){
//
//            }
        };
    }


    public static void pushToRedis(CustomerRequest customerRequest){
        log.info("start connect to jedis");
        Jedis jedis = new Jedis("http://localhost:6379");
        log.info("connect to jedis succeful");
        jedis.lpush(customerRequest.getToken(), customerRequest.getJson());
    }

    public static void pushtoDatabase(CustomerRequest customerRequest) throws SQLException {

        String data = customerRequest.getJson();

        JSONObject jsonObject = new JSONObject(data);
        String customerName = jsonObject.getString("customerName");
        int rescode =  jsonObject.getInt("rescode");
        int amount = jsonObject.getInt("amount");
        int debitAmount = jsonObject.getInt("debitAmount");
        int realAmount = jsonObject.getInt("realAmount");
        String payDate = jsonObject.getString("payDate");
        String sysdate = jsonObject.getString("sysdate");

        //  insert to database
        String sql = "CALL PKG_CUSTOMER.ADD_NEW_CUSTOMER(?,?,?,?,?,?,?,?)";
        PreparedStatement st = oracleConn.prepareStatement(sql);
        st.setString(1, sysdate);
        st.setString(2, payDate);
        st.setLong(3, realAmount);
        st.setLong(4, debitAmount);
        st.setString(5, data);
        st.setLong(6, rescode);
        st.setString(7, customerName);



    }


}
