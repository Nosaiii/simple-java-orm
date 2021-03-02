package com.nosaiii.sjorm;

import com.nosaiii.sjorm.exceptions.ModelMetadataNotRegisteredException;
import com.nosaiii.sjorm.exceptions.NoParameterlessConstructorException;
import com.nosaiii.sjorm.metadata.AbstractModelMetadata;
import com.nosaiii.sjorm.metadata.ModelMetadata;
import com.nosaiii.sjorm.querybuilder.QueryBuilder;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;

public class SJORM {
    private static SJORM instance;

    private final SJORMConnection connection;

    @SuppressWarnings("SpellCheckingInspection")
    private final HashMap<Class<? extends Model>, AbstractModelMetadata> metadatas;

    /**
     * Bounds the SJORM service to the static instance
     * @param host The address of the database server
     * @param port The port of the database server
     * @param database The name of the database
     * @param username The username of the login to connect to the database server
     * @param password The password of the login to connect to the database server
     * @return A new {@link SJORM} instance. This instance can later be accessed by using the static method {@code SJORM.getInstance()}
     */
    public static SJORM register(String host, int port, String database, String username, String password) {
        return instance = new SJORM(host, port, database, username, password);
    }

    /**
     * The singleton registered instance of the SJORM service
     * @return
     */
    public static SJORM getInstance() {
        return instance;
    }

    private SJORM(String host, int port, String database, String username, String password) {
        connection = new SJORMConnection(host, port, database, username, password);
        metadatas = new HashMap<>();
    }

    /**
     * Bounds a model to the SJORM service using the given metadata of the model
     * @param metadata The metadata of the model to bound to the service
     */
    public void registerModel(AbstractModelMetadata metadata) {
        metadatas.put(metadata.getType(), metadata);
    }

    /**
     * Queries all entries of the given model from the database
     * @param modelClass The class type of the model to retrieve
     * @param <T> The type of the model to retrieve
     * @return A {@link Query} object containing instances of models from the database
     * @throws ModelMetadataNotRegisteredException Thrown when the given class type of the model was not bound to the SJORM service
     */
    public <T extends Model> Query<T> getAll(Class<T> modelClass) throws ModelMetadataNotRegisteredException {
        return getLimit(modelClass, -1);
    }

    /**
     * Queries a limited amount of entries of the given model from the database
     * @param modelClass The class type of the model to retrieve
     * @param limit The limit of model instances to retrieve
     * @param <T> The type of the model to retrieve
     * @return A {@link Query} object containing instances of models from the database
     * @throws ModelMetadataNotRegisteredException Thrown when the given class type of the model was not bound to the SJORM service
     */
    public <T extends Model> Query<T> getLimit(Class<T> modelClass, int limit) throws ModelMetadataNotRegisteredException {
        if(!metadatas.containsKey(modelClass)) {
            throw new ModelMetadataNotRegisteredException(modelClass);
        }

        AbstractModelMetadata metadata = metadatas.get(modelClass);

        QueryBuilder builder = new QueryBuilder(connection.getConnection())
                .select()
                .from(metadata.getTable());

        if(limit > 0) {
            builder = builder.limit(limit);
        }

        ResultSet resultSet = builder.executeQuery();
        try {
            return new Query<>(resultSet, modelClass);
        } catch (NoParameterlessConstructorException e) {
            e.printStackTrace();
        }

        return new Query<>(new ArrayList<>());
    }

    /**
     * Gets the metadata from the service by the given class type of the model
     * @param modelClass The class type of the model to retrieve the metadata from
     * @return A {@link ModelMetadata} object containing metadata of a model
     * @throws ModelMetadataNotRegisteredException Thrown when the given class type of the model was not bound to the SJORM service
     */
    public AbstractModelMetadata getMetadata(Class<? extends Model> modelClass) throws ModelMetadataNotRegisteredException {
        if(!metadatas.containsKey(modelClass)) {
            throw new ModelMetadataNotRegisteredException(modelClass);
        }

        return metadatas.get(modelClass);
    }

    /**
     * Gets the {@link SJORMConnection} instance containing extended functionality from the data-access layer
     * @return The {@link SJORMConnection} instance containing extended functionality from the data-access layer
     */
    public SJORMConnection getSJORMConnection() {
        return connection;
    }
}