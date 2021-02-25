package main.java.com.nosaiii.sjorm;

import main.java.com.nosaiii.sjorm.utility.SQLUtility;

import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

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

    /**
     * Execute plain SQL result-giving queries to the database
     * @param sql The SQL query to execute
     * @param params (Optional) parameters to be used by the query, replacing '?' properties
     * @return A {@link List} of {@link LinkedHashMap} objects containing data from the executed query result
     */
    private List<LinkedHashMap<String, Object>> query(String sql, Object... params) {
        List<LinkedHashMap<String, Object>> result = new ArrayList<>();

        try(PreparedStatement statement = connection.prepareStatement(sql)) {
            for(int i = 1; i <= params.length; i++) {
                statement.setObject(i, params[i - 1]);
            }

            ResultSet resultSet = statement.executeQuery();
            ResultSetMetaData metaData = resultSet.getMetaData();

            while(resultSet.next()) {
                LinkedHashMap<String, Object> row = new LinkedHashMap<>();

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

    /**
     * Gets the names of the column from the given table in the database that are present as a primary key field
     * @param table The name of the table in the database
     * @return An array of column names that are primary key fields in the given table of the database
     */
    public String[] getPrimaryKeys(String table) {
        String query = "DESCRIBE " + SQLUtility.quote(table);
        List<LinkedHashMap<String, Object>> results = query(query);

        return results.stream()
                .filter(row -> row.get("Key").equals("PRI"))
                .map(row -> row.get("Field")).toArray(String[]::new);
    }

    /**
     * The address of the database server
     * @return The address of the database server
     */
    public String getHost() {
        return host;
    }

    /**
     * The port of the database server
     * @return The port of the database server
     */
    public int getPort() {
        return port;
    }

    /**
     * The name of the database
     * @return The name of the database
     */
    public String getDatabase() {
        return database;
    }

    /**
     * The username of the login to connect to the database server
     * @return The username of the login to connect to the database server
     */
    public String getUsername() {
        return username;
    }

    /**
     * The SQL connection used to execute queries on
     * @return The {@link Connection} object used to execute queries on
     */
    public Connection getConnection() {
        return connection;
    }
}