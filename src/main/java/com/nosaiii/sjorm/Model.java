package main.java.com.nosaiii.sjorm;

import main.java.com.nosaiii.sjorm.exceptions.ModelMetadataNotRegisteredException;
import main.java.com.nosaiii.sjorm.utility.SqlUtility;

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

        StringBuilder queryBuilder = new StringBuilder();

        if(!isNew) {
            List<String> setStatements = new ArrayList<>();
            for(String propertyKey : properties.keySet()) {
                setStatements.add(propertyKey + " = ?");
            }
            String setStatement = String.join(", ", setStatements);

            List<String> whereStatements = new ArrayList<>();
            for(String primaryKey : cachedPrimaryKeyValues.keySet()) {
                whereStatements.add(primaryKey + " = ?");
            }
            String whereStatement = String.join(" AND ", whereStatements);

            queryBuilder.append("UPDATE ").append(metadata.getTable()).append(" SET ");
            queryBuilder.append(setStatement).append(" ");
            queryBuilder.append("WHERE ");
            queryBuilder.append(whereStatement);

            try {
                PreparedStatement statement = connection.prepareStatement(queryBuilder.toString());

                // Property binding in SET
                List<String> propertyKeys = new ArrayList<>(properties.keySet());
                for(int i = 1; i <= propertyKeys.size(); i++) {
                    statement.setObject(i, properties.get(propertyKeys.get(i - 1)));
                }

                // Property binding in WHERE
                List<Object> primaryKeyValues = new ArrayList<>(cachedPrimaryKeyValues.values());
                for(int i = 1; i <= cachedPrimaryKeyValues.size(); i++) {
                    int offsetIndex = propertyKeys.size() + 1;
                    statement.setObject(offsetIndex, primaryKeyValues.get(i - 1));
                }

                statement.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            // Return when no properties are present to save
            if(properties.isEmpty()) {
                return;
            }

            String[] parameterCharacters = new String[properties.size()];
            Arrays.fill(parameterCharacters, "?");

            queryBuilder.append("INSERT INTO ").append(SqlUtility.quote(metadata.getTable())).append(" ");
            queryBuilder.append("(").append(SqlUtility.quote(properties.keySet())).append(") ");
            queryBuilder.append("VALUES ");
            queryBuilder.append("(").append(String.join(", ", parameterCharacters)).append(")");

            try {
                PreparedStatement statement = connection.prepareStatement(queryBuilder.toString());

                // Property binding in INSERT
                List<String> propertyKeys = new ArrayList<>(properties.keySet());
                for(int i = 1; i <= propertyKeys.size(); i++) {
                    statement.setObject(i, properties.get(propertyKeys.get(i - 1)));
                }

                statement.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
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