package main.java.com.nosaiii.sjorm;

import main.java.com.nosaiii.sjorm.querybuilder.QueryBuilder;
import main.java.com.nosaiii.sjorm.querybuilder.condition.SQLBasicCondition;
import main.java.com.nosaiii.sjorm.querybuilder.condition.SQLConditionType;
import main.java.com.nosaiii.sjorm.utility.SQLUtility;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.ResultSetMetaData;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

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
     * Gets the names of the column from the given table in the database that are present as a primary key field
     * @param table The name of the table in the database
     * @return An array of column names that are primary key fields in the given table of the database
     */
    public String[] getPrimaryKeys(String table) {
        QueryBuilder builder = new QueryBuilder(connection)
                .sql("DESCRIBE " + SQLUtility.quote(table));

        try {
            List<LinkedHashMap<String, Object>> results = convertToMap(builder.executeQuery());
            return results.stream()
                    .filter(row -> row.get("Key").equals("PRI"))
                    .map(row -> row.get("Field")).toArray(String[]::new);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Constructs a defined map of key-value pairs where column names refer to a its foreign key reference, including its table name and column name
     * @param table The name of the table to get its foreign key references from
     * @return A defined map of key-value pairs where column names refer to a its foreign key reference, including its table name and column name
     */
    public Map<String, ForeignKeyReference> getForeignKeyReferences(String table) {
        Map<String, ForeignKeyReference> references = new HashMap<>();

        QueryBuilder builder = new QueryBuilder(connection)
                .select("COLUMN_NAME", "REFERENCED_TABLE_NAME", "REFERENCED_COLUMN_NAME")
                .from("INFORMATION_SCHEMA", "KEY_COLUMN_USAGE")
                .where(new SQLBasicCondition("REFERENCED_TABLE_SCHEMA", SQLConditionType.EQUALS, database))
                .and(new SQLBasicCondition("TABLE_NAME", SQLConditionType.EQUALS, table));

        try(ResultSet resultSet = builder.executeQuery()) {
            while(resultSet.next()) {
                ForeignKeyReference reference = new ForeignKeyReference(resultSet.getString(2), resultSet.getString(3));
                references.put(resultSet.getString(1), reference);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return references;
    }

    /**
     * Converts {@link ResultSet} object to a {@link List} object with {@link LinkedHashMap} representing a table of data
     * @param resultSet The {@link ResultSet} object to convert
     * @return An instance of a {@link List} with {@link LinkedHashMap} objects representing a table of data
     * @throws SQLException Thrown when an error occured trying to retrieve metadata from the result set
     */
    public List<LinkedHashMap<String, Object>> convertToMap(ResultSet resultSet) throws SQLException {
        List<LinkedHashMap<String, Object>> result = new ArrayList<>();
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

        return result;
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