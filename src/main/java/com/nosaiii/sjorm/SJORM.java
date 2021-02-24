package main.java.com.nosaiii.sjorm;

import main.java.com.nosaiii.sjorm.exceptions.ModelMetadataNotRegisteredException;
import main.java.com.nosaiii.sjorm.exceptions.NoParameterlessConstructorException;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

public class SJORM {
    private static SJORM instance;

    private final SJORMConnection connection;

    @SuppressWarnings("SpellCheckingInspection")
    private final HashMap<Class<? extends Model>, ModelMetadata> metadatas;

    public static SJORM register(String host, int port, String database, String username, String password) {
        return instance = new SJORM(host, port, database, username, password);
    }

    public static SJORM getInstance() {
        return instance;
    }

    private SJORM(String host, int port, String database, String username, String password) {
        connection = new SJORMConnection(host, port, database, username, password);

        metadatas = new HashMap<>();
    }

    public void registerModel(ModelMetadata metadata) {
        metadatas.put(metadata.getType(), metadata);
    }

    public <T extends Model> Query<T> getAll(Class<T> modelClass) throws ModelMetadataNotRegisteredException {
        return getLimit(modelClass, -1);
    }

    public <T extends Model> Query<T> getLimit(Class<T> modelClass, int limit) throws ModelMetadataNotRegisteredException {
        if(!metadatas.containsKey(modelClass)) {
            throw new ModelMetadataNotRegisteredException(modelClass);
        }

        ModelMetadata metadata = metadatas.get(modelClass);

        String query = "SELECT * FROM " + metadata.getTable();
        if(limit > 0) {
            query += " LIMIT" + limit;
        }

        return getQueryFromSQL(query, modelClass);
    }

    private <T extends Model> Query<T> getQueryFromSQL(String query, Class<T> modelClass) {
        try {
            PreparedStatement statement = connection.getConnection().prepareStatement(query);
            ResultSet resultSet = statement.executeQuery();
            return new Query<>(resultSet, modelClass);
        } catch (SQLException | NoParameterlessConstructorException e) {
            e.printStackTrace();
        }

        return new Query<>(new ArrayList<>());
    }

    public ModelMetadata getMetadata(Class<? extends Model> modelClass) throws ModelMetadataNotRegisteredException {
        if(!metadatas.containsKey(modelClass)) {
            throw new ModelMetadataNotRegisteredException(modelClass);
        }

        return metadatas.get(modelClass);
    }

    public SJORMConnection getSJORMConnection() {
        return connection;
    }
}