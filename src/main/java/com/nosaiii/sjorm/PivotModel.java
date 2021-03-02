package com.nosaiii.sjorm;

import java.sql.ResultSet;

public class PivotModel extends Model {
    private final Class<? extends Model> classLeft;
    private final Class<? extends Model> classRight;

    public PivotModel(ResultSet resultSet, Class<? extends Model> classLeft, Class<? extends Model> classRight) {
        super(resultSet);
        this.classLeft = classLeft;
        this.classRight = classRight;
    }

    public PivotModel(Class<? extends Model> classLeft, Class<? extends Model> classRight) {
        this.classLeft = classLeft;
        this.classRight = classRight;
    }

    /**
     * Gets the class type of the model on the left side of the pivot table
     * @return The class type of the model on the left side of the pivot table
     */
    public Class<? extends Model> getClassLeft() {
        return classLeft;
    }

    /**
     * Gets the class type of the model on the right side of the pivot table
     * @return The class type of the model on the right side of the pivot table
     */
    public Class<? extends Model> getClassRight() {
        return classRight;
    }
}