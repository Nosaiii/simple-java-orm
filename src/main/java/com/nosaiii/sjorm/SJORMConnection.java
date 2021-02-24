package main.java.com.nosaiii.sjorm;

import main.java.com.nosaiii.sjorm.utility.SqlUtility;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

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

    private List<HashMap<String, Object>> query(String sql, Object... params) {
        List<HashMap<String, Object>> result = new ArrayList<>();

        try(PreparedStatement statement = connection.prepareStatement(sql)) {
            for(int i = 1; i <= params.length; i++) {
                statement.setObject(i, params[i - 1]);
            }

            ResultSet resultSet = statement.executeQuery();
            ResultSetMetaData metaData = resultSet.getMetaData();

            while(resultSet.next()) {
                HashMap<String, Object> row = new HashMap<>();

                for(int i = 1; i <= metaData.getColumnCount(); i++) {
                    String columnName = metaData.getColumnName(i);
                    Object columnValue = resultSet.getObject(i);

                    row.put(columnName, columnValue);
                }

                result.add(row);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return result;
    }

    public String[] getPrimaryKeys(String table) {
        String query = "DESCRIBE " + SqlUtility.quote(table);
        List<HashMap<String, Object>> results = query(query);

        return results.stream()
                .filter(row -> row.get("Key").equals("PRI"))
                .map(row -> row.get("Field")).toArray(String[]::new);
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