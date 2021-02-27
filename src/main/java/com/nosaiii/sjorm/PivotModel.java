package main.java.com.nosaiii.sjorm;

import java.sql.ResultSet;

public class PivotModel<Left extends Model, Right extends Model> extends Model {
    private final Class<Left> classLeft;
    private final Class<Right> classRight;

    public PivotModel(ResultSet resultSet, Class<Left> classLeft, Class<Right> classRight) {
        super(resultSet);
        this.classLeft = classLeft;
        this.classRight = classRight;
    }

    public PivotModel(Class<Left> classLeft, Class<Right> classRight) {
        this.classLeft = classLeft;
        this.classRight = classRight;
    }

    /**
     * Gets the class type of the model on the left side of the pivot table
     * @return The class type of the model on the left side of the pivot table
     */
    public Class<Left> getClassLeft() {
        return classLeft;
    }

    /**
     * Gets the class type of the model on the right side of the pivot table
     * @return The class type of the model on the right side of the pivot table
     */
    public Class<Right> getClassRight() {
        return classRight;
    }
}