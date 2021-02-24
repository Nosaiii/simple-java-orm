package main.java.com.nosaiii.sjorm.exceptions;

import main.java.com.nosaiii.sjorm.Model;

public class NoParameterlessConstructorException extends Exception {
    public NoParameterlessConstructorException(Class<? extends Model> modelClass) {
        super("No parameterless constructor present in the model '" + modelClass.getSimpleName() + "'");
    }
}