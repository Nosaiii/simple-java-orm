package main.java.com.nosaiii.sjorm;

import main.java.com.nosaiii.sjorm.exceptions.ModelMetadataNotRegisteredException;
import main.java.com.nosaiii.sjorm.exceptions.NoSuchPropertyException;

import java.sql.*;
import java.util.HashMap;

public abstract class Model {
    private final HashMap<String, Object> properties;
    private final ModelMetadata metadata;

    Model(ResultSet resultSet) throws ModelMetadataNotRegisteredException {
        properties = new HashMap<>();
        metadata = SJORM.getInstance().getMetadata(getClass());

        try {
            ResultSetMetaData resultSetMetaData = resultSet.getMetaData();

            for (int i = 1; i <= resultSetMetaData.getColumnCount(); i++) {
                String columnName = resultSetMetaData.getColumnName(i);
                properties.put(columnName, resultSet.getObject(i));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Model() throws ModelMetadataNotRegisteredException {
        properties = new HashMap<>();
        metadata = SJORM.getInstance().getMetadata(getClass());

        Connection connection = SJORM.getInstance().getSJORMConnection().getConnection();
        String query = "SHOW COLUMNS FROM " + metadata.getTable();

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            ResultSet resultSet = preparedStatement.executeQuery();

            while(resultSet.next()) {
                properties.put(resultSet.getString("Field"), null);
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public void setProperty(String column, Object value) throws NoSuchPropertyException {
        if(!properties.containsKey(column)) {
            throw new NoSuchPropertyException(column, this);
        }

        properties.put(column, value);
    }

    public Object getProperty(String column) throws NoSuchPropertyException {
        if(!properties.containsKey(column)) {
            throw new NoSuchPropertyException(column, this);
        }

        return properties.get(column);
    }

    public <T> T getProperty(String column, Class<T> clazz) throws NoSuchPropertyException {
        return clazz.cast(getProperty(column));
    }
}