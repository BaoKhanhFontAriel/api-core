package org.example;


import oracle.jdbc.OracleDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class OracleConnect {

    Logger log = LoggerFactory.getLogger(OracleConnect.class);
    Connection conn = null;

    private static OracleConnect instance = new OracleConnect();

    public OracleConnect() {
        try {
            DriverManager.registerDriver(new OracleDriver());

            String dbURL = "jdbc:oracle:thin:@10.22.19.192:1521:OFFLINE";
            String username = "NHSVBAOFF";
            String password = "NHSVBAOFF";
            conn = DriverManager.getConnection(dbURL, username, password);

            if (conn != null) {
                System.out.println("Connect to oracle succesful!");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static  OracleConnect getInstance(){
        if (instance == null){
            instance = new OracleConnect();
        }
        return instance;
    }

    public Connection getConn() {
        return conn;
    }
}
