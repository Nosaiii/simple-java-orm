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

    /**
     * Constructor on retrieving an existing model from the database
     * @param resultSet The {@link ResultSet} object containing the data from the databse of this model
     */
    protected Model(ResultSet resultSet) {
        properties = new LinkedHashMap<>();
        metadata = SJORM.getInstance().getMetadata(getClass());

        isNew = false;

        cachedPrimaryKeyValues = new LinkedHashMap<>();

        try {
            ResultSetMetaData resultSetMetaData = resultSet.getMetaData();

            for (int i = 1; i <= resultSetMetaData.getColumnCount(); i++) {
                String columnName = resultSetMetaData.getColumnName(i);
                Object columnValue = resultSet.getObject(i);

                properties.put(columnName, columnValue);

                if(Arrays.asList(metadata.getPrimaryKeyFields()).contains(columnName)) {
                    cachedPrimaryKeyValues.put(columnName, columnValue);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Constructor on creating a new non-existing instance of this model to be saved in the database
     */
    public Model() {
        properties = new LinkedHashMap<>();
        metadata = SJORM.getInstance().getMetadata(getClass());

        isNew = true;

        cachedPrimaryKeyValues = new LinkedHashMap<>();
    }

    /**
     * Saves the model to the database
     */
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

    /**
     * Sets a property of the model. The name of the property must be existing in the database.
     * @param column The name of the property (or column in the database)
     * @param value The value to give to the property
     */
    public void setProperty(String column, Object value) {
        properties.put(column, value);
    }

    /**
     * Gets the value of a property of the model, given by the name of the property
     * @param column The name of the property (or column in the database)
     * @return The value of a property of the model, given by the name of the property
     */
    public Object getProperty(String column) {
        return properties.get(column);
    }

    /**
     * Gets the value of a property of the model, given by the name of the property and casts it into the desired type
     * @param column The name of the property (or column in the database)
     * @param clazz The class of the type to attempt to cast the property to
     * @param <T> The type to attempt to cast the property to
     * @return The value of a property of the model, given by the name of the property and casts it into the desired type
     */
    public <T> T getProperty(String column, Class<T> clazz) {
        return clazz.cast(getProperty(column));
    }
}