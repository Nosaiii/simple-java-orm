package main.java.com.nosaiii.sjorm;

import main.java.com.nosaiii.sjorm.exceptions.NoParameterlessConstructorException;
import main.java.com.nosaiii.sjorm.metadata.AbstractModelMetadata;
import main.java.com.nosaiii.sjorm.metadata.ModelMetadata;
import main.java.com.nosaiii.sjorm.metadata.PivotModelMetadata;
import main.java.com.nosaiii.sjorm.querybuilder.QueryBuilder;
import main.java.com.nosaiii.sjorm.querybuilder.SQLPair;
import main.java.com.nosaiii.sjorm.querybuilder.condition.SQLBasicCondition;
import main.java.com.nosaiii.sjorm.querybuilder.condition.SQLConditionType;

import java.sql.*;
import java.util.*;

public abstract class Model {
    private final LinkedHashMap<String, Object> properties;
    private final AbstractModelMetadata metadata;

    private boolean isNew;

    private final LinkedHashMap<String, Object> cachedPrimaryKeyValues;

    /**
     * Constructor on retrieving an existing model from the database
     * @param resultSet The {@link ResultSet} object containing the data from the databse of this model
     */
    public Model(ResultSet resultSet) {
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

    /**
     * Gets a collection of related models, given by their class type, instances that are related to this model
     * @param targetModelClass The class type to get the related models from
     * @param foreignKeyColumns (Optional) foreign key columns if defined different than the auto generated foreign key column names to look the related models for
     * @param <T> The type to get the related models from
     * @return A {@link Query} collection containing the related models of this model
     */
    public <T extends Model> Query<T> hasMany(Class<T> targetModelClass, String... foreignKeyColumns) {
        // Automatically get names of foreign key columns if not set
        if(foreignKeyColumns.length == 0) {
            foreignKeyColumns = getAutoGeneratedForeignKeyColumns(metadata);
        }

        String targetTableName = SJORM.getInstance().getMetadata(targetModelClass).getTable();

        // Construct base query
        QueryBuilder builder = new QueryBuilder(SJORM.getInstance().getSJORMConnection().getConnection())
                .select()
                .from(targetTableName);

        // Add WHERE statements to get the correct rows
        Map<String, ForeignKeyReference> foreignKeyReference = SJORM.getInstance().getSJORMConnection().getForeignKeyReferences(targetTableName);

        for(int i = 0; i < foreignKeyColumns.length; i++) {
            String foreignKeyColumn = foreignKeyColumns[i];
            String sourceColumn = foreignKeyReference.get(foreignKeyColumn).getColumn();

            SQLBasicCondition condition = new SQLBasicCondition(foreignKeyColumn, SQLConditionType.EQUALS, getProperty(sourceColumn));
            if(i == 0) {
                builder = builder.where(condition);
            } else {
                builder = builder.and(condition);
            }
        }

        // Construct a Query<T> object from the result set of the query
        try(ResultSet resultSet = builder.executeQuery()) {
            return new Query<>(resultSet, targetModelClass);
        } catch (SQLException | NoParameterlessConstructorException e) {
            e.printStackTrace();
        }

        // Return an empty query if something went wrong
        return new Query<>(new ArrayList<>());
    }

    /**
     * Gets the model, given by its class type, this model is bound to using its foreign key(s)
     * @param targetModelClass The class type to get its related model from
     * @param foreignKeyColumns (Optional) foreign key columns if defined different than the auto generated foreign key column names to look the related model for
     * @param <T> The type to get its related model from
     * @return A {@link Model} instance of the given model class type that this model is related to
     */
    public <T extends Model> T belongsTo(Class<T> targetModelClass, String... foreignKeyColumns) {
        // Get metadata of target model class
        AbstractModelMetadata targetMetadata = SJORM.getInstance().getMetadata(targetModelClass);

        // Automatically get names of foreign key columns if not set
        if(foreignKeyColumns.length == 0) {
            foreignKeyColumns = getAutoGeneratedForeignKeyColumns(targetMetadata);
        }

        // Get the table name of the target model class
        String targetTableName = SJORM.getInstance().getMetadata(targetModelClass).getTable();

        // Construct base query
        QueryBuilder builder = new QueryBuilder(SJORM.getInstance().getSJORMConnection().getConnection())
                .select()
                .from(targetTableName);

        // Add WHERE statements to get the correct rows
        Map<String, ForeignKeyReference> foreignKeyReference = SJORM.getInstance().getSJORMConnection().getForeignKeyReferences(metadata.getTable());

        for(int i = 0; i < foreignKeyColumns.length; i++) {
            String foreignKeyColumn = foreignKeyColumns[i];
            String targetColumn = foreignKeyReference.get(foreignKeyColumn).getColumn();

            SQLBasicCondition condition = new SQLBasicCondition(targetColumn, SQLConditionType.EQUALS, getProperty(foreignKeyColumn));
            if(i == 0) {
                builder = builder.where(condition);
            } else {
                builder = builder.and(condition);
            }
        }

        // Construct a Query<T> object from the result set of the query and get the first entry
        try(ResultSet resultSet = builder.executeQuery()) {
            return new Query<>(resultSet, targetModelClass).first();
        } catch (SQLException | NoParameterlessConstructorException e) {
            e.printStackTrace();
        }

        // Return null if something went wrong
        return null;
    }

    public <T extends Model, P extends PivotModel> Query<T> hasManyPivot(Class<P> pivotModelClass) {
        return hasManyPivot(pivotModelClass, new String[0], new String[0]);
    }

    public <T extends Model, P extends PivotModel> Query<T> hasManyPivot(Class<P> pivotModelClass, String[] sourceForeignKeyColumns, String[] targetForeignKeyColumns) {
        // Get metadata of target model class
        PivotModelMetadata targetPivotMetadata = (PivotModelMetadata) SJORM.getInstance().getMetadata(pivotModelClass);

        // Automatically get names of foreign key columns if not set
        if(sourceForeignKeyColumns.length == 0) {
            String tableName = null;
            String[] primaryKeyFields = null;

            if(targetPivotMetadata.getTypeLeft().equals(getClass())) {
                tableName = targetPivotMetadata.getTableLeft();
                primaryKeyFields = targetPivotMetadata.getPrimaryKeyFieldsLeft();
            } else {
                tableName = targetPivotMetadata.getTableRight();
                primaryKeyFields = targetPivotMetadata.getPrimaryKeyFieldsRight();
            }

            sourceForeignKeyColumns = getAutoGeneratedPivotForeignKeyColumns(tableName, primaryKeyFields);
        }

        if(targetForeignKeyColumns.length == 0) {
            String tableName = null;
            String[] primaryKeyFields = null;

            if(targetPivotMetadata.getTypeRight().equals(getClass())) {
                tableName = targetPivotMetadata.getTableLeft();
                primaryKeyFields = targetPivotMetadata.getPrimaryKeyFieldsLeft();
            } else {
                tableName = targetPivotMetadata.getTableRight();
                primaryKeyFields = targetPivotMetadata.getPrimaryKeyFieldsRight();
            }

            targetForeignKeyColumns = getAutoGeneratedPivotForeignKeyColumns(tableName, primaryKeyFields);
        }

        // Construct base query
        QueryBuilder builder = new QueryBuilder(SJORM.getInstance().getSJORMConnection().getConnection())
                .select()
                .from(targetPivotMetadata.getTable());

        // Add WHERE statements to get the correct rows
        Map<String, ForeignKeyReference> foreignKeyReference = SJORM.getInstance().getSJORMConnection().getForeignKeyReferences(targetPivotMetadata.getTable());

        for(int i = 0; i < sourceForeignKeyColumns.length; i++) {
            String foreignKeyColumn = sourceForeignKeyColumns[i];
            String sourceColumn = foreignKeyReference.get(foreignKeyColumn).getColumn();

            SQLBasicCondition condition = new SQLBasicCondition(foreignKeyColumn, SQLConditionType.EQUALS, getProperty(sourceColumn));
            if(i == 0) {
                builder = builder.where(condition);
            } else {
                builder = builder.and(condition);
            }
        }

        // Construct a Query<P> object from the result set of the query
        Query<P> pivotModels = new Query<>(new ArrayList<>());
        try(ResultSet resultSet = builder.executeQuery()) {
            pivotModels = new Query<>(resultSet, pivotModelClass);
        } catch (SQLException | NoParameterlessConstructorException e) {
            e.printStackTrace();
        }

        // Construct a new Query<T> with the joined target table
        List<T> targetModelsList = new ArrayList<>();
        for(PivotModel pivotModel : pivotModels.toList()) {
            T targetModel = null;

            if(pivotModel.getClassLeft().equals(getClass())) {
                //noinspection unchecked
                targetModel = pivotModel.belongsTo((Class<T>) pivotModel.getClassRight(), targetForeignKeyColumns);
            } else {
                //noinspection unchecked
                targetModel = pivotModel.belongsTo((Class<T>) pivotModel.getClassLeft(), targetForeignKeyColumns);
            }

            targetModelsList.add(targetModel);
        }

        return new Query<>(targetModelsList);
    }

    /**
     * Automatically generates the column names for foreign keys by using the names of the primary keys
     * @param targetMetadata The metadata used to construct the auto-generated foreign key column names
     * @return The auto-generated foreign key column names
     */
    private String[] getAutoGeneratedForeignKeyColumns(AbstractModelMetadata targetMetadata) {
        return Arrays.stream(targetMetadata.getPrimaryKeyFields())
                .map(pk -> targetMetadata.getTable() + "_" + pk)
                .toArray(String[]::new);
    }

    /**
     * Automatically generates the column names for foreign keys by using the names of the primary keys
     * @param tableName The table name, used to prefix the auto-generated foreign key column names
     * @param primaryKeyFields The collection of string defining the primary key fields of the source table, to be used in the pivot table
     * @return The auto-generated foreign key column names
     */
    private String[] getAutoGeneratedPivotForeignKeyColumns(String tableName, String[] primaryKeyFields) {
        return Arrays.stream(primaryKeyFields)
                .map(pk -> tableName + "_" + pk)
                .toArray(String[]::new);
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder("[" + getClass().getSimpleName() + "] ");

        List<String> properties = new ArrayList<>();
        for(Map.Entry<String, Object> property : this.properties.entrySet()) {
            properties.add(property.getKey() + ":" + property.getValue());
        }
        stringBuilder.append(String.join("|", properties));

        return stringBuilder.toString();
    }
}