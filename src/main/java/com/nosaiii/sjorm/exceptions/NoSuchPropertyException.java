package main.java.com.nosaiii.sjorm.exceptions;

import main.java.com.nosaiii.sjorm.Model;

public class NoSuchPropertyException extends Exception {
    public NoSuchPropertyException(String column, Model model) {
        super("The property '" + column + "' could not be found in the model '" + model.getClass().getSimpleName() + "'");
    }
}