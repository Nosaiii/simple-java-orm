package main.java.com.nosaiii.sjorm;

import main.java.com.nosaiii.sjorm.exceptions.ModelMetadataNotRegisteredException;
import main.java.com.nosaiii.sjorm.querybuilder.QueryBuilder;
import main.java.com.nosaiii.sjorm.querybuilder.SQLPair;
import main.java.com.nosaiii.sjorm.querybuilder.condition.SQLBasicCondition;
import main.java.com.nosaiii.sjorm.querybuilder.condition.SQLConditionType;

import java.sql.*;
import java.util.*;

public abstract class Model {
    private final LinkedHashMap<String, Object> properties;
    private final ModelMetadata metadata;

    private boolean isNew;

    private final LinkedHashMap<String, Object> cachedPrimaryKeyValues;

    protected Model(ResultSet resultSet) throws ModelMetadataNotRegisteredException {
        properties = new LinkedHashMap<>();
        metadata = SJORM.getInstance().getMetadata(getClass());

        isNew = false;

        cachedPrimaryKeyValues = new LinkedHashMap<>();

        try {
            ResultSetMetaData resultSetMetaData = resultSet.getMetaData();

            for (int i = 1; i <= resultSetMetaData.getColumnCount(); i++) {
                String columnName = resultSetMetaData.getColumnName(i);
                Object columnValue = resultSet.getObject(i);

                populateProperty(columnName, columnValue);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Model() throws ModelMetadataNotRegisteredException {
        properties = new LinkedHashMap<>();
        metadata = SJORM.getInstance().getMetadata(getClass());

        isNew = true;

        cachedPrimaryKeyValues = new LinkedHashMap<>();
    }

    private void populateProperty(String name, Object value) {
        properties.put(name, value);

        if(Arrays.asList(metadata.getPrimaryKeyFields()).contains(name)) {
            cachedPrimaryKeyValues.put(name, value);
        }
    }

    public void save() {
        Connection connection = SJORM.getInstance().getSJORMConnection().getConnection();

        if(!isNew) {
            List<SQLPair> pairs = new ArrayList<>();
            for(Map.Entry<String, Object> propertyEntry : properties.entrySet()) {
                pairs.add(new SQLPair(propertyEntry.getKey(), propertyEntry.getValue()));
            }
            SQLPair[] pairArray = pairs.toArray(new SQLPair[0]);

            QueryBuilder builder = new QueryBuilder(connection)
                    .update(metadata.getTable(), pairArray);

            for(Map.Entry<String, Object> propertyEntry : cachedPrimaryKeyValues.entrySet()) {
                SQLBasicCondition condition = new SQLBasicCondition(propertyEntry.getKey(), SQLConditionType.EQUALS, propertyEntry.getValue());
                builder = builder.where(condition);
            }

            builder.executeUpdate();
        } else {
            // Return when no properties are present to save
            if(properties.isEmpty()) {
                return;
            }

            QueryBuilder builder = new QueryBuilder(connection);
            builder
                    .insertInto(metadata.getTable(), properties.keySet())
                    .values(properties.values());

            builder.executeUpdate();
        }

        isNew = false;
    }

    public void setProperty(String column, Object value) {
        properties.put(column, value);
    }

    public Object getProperty(String column) {
        return properties.get(column);
    }

    public <T> T getProperty(String column, Class<T> clazz) {
        return clazz.cast(getProperty(column));
    }
}