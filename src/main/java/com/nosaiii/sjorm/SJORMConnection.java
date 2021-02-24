package main.java.com.nosaiii.sjorm;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SJORMConnection {
    private String host;
    private int port;
    private String database;
    private String username;

    private Connection connection;

    SJORMConnection(String host, int port, String database, String username, String password) {
        this.host = host;
        this.port = port;
        this.database = database;
        this.username = username;

        try {
            Class.forName("com.mysql.jdbc.Driver");

            String uri = "jdbc:mysql://" + host + ":" + port + "/" + database + "?autoReconnect=true&useSSL=false&characterEncoding=utf8";
            connection = DriverManager.getConnection(uri, username, password);
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getDatabase() {
        return database;
    }

    public String getUsername() {
        return username;
    }

    public Connection getConnection() {
        return connection;
    }
}