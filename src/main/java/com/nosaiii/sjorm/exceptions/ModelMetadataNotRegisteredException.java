package main.java.com.nosaiii.sjorm.exceptions;

import main.java.com.nosaiii.sjorm.Model;

public class ModelMetadataNotRegisteredException extends RuntimeException {
    public ModelMetadataNotRegisteredException(Class<? extends Model> modelClass) {
        super("The model metadata was not bound for the model '" + modelClass.getSimpleName() + "'");
    }
}