package com.nosaiii.sjorm.exceptions;

import com.nosaiii.sjorm.Model;

public class NoParameterlessConstructorException extends Exception {
    public NoParameterlessConstructorException(Class<? extends Model> modelClass) {
        super("No parameterless constructor present in the model '" + modelClass.getSimpleName() + "'");
    }
}