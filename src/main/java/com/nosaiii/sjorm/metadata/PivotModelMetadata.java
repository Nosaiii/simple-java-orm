package main.java.com.nosaiii.sjorm.metadata;

import main.java.com.nosaiii.sjorm.Model;
import main.java.com.nosaiii.sjorm.PivotModel;
import main.java.com.nosaiii.sjorm.SJORM;
import main.java.com.nosaiii.sjorm.annotations.SJORMTable;

public class PivotModelMetadata extends AbstractModelMetadata {
    private final Class<? extends Model> typeLeft;
    private final Class<? extends Model> typeRight;

    private final String tableLeft;
    private final String tableRight;

    private final String[] primaryKeyFieldsLeft;
    private final String[] primaryKeyFieldsRight;

    public PivotModelMetadata(Class<? extends PivotModel> type, Class<? extends Model> typeLeft, Class<? extends Model> typeRight) {
        super(type);

        this.typeLeft = typeLeft;
        this.typeRight = typeRight;

        this.tableLeft = typeLeft.getAnnotation(SJORMTable.class).tableName();
        this.tableRight = typeRight.getAnnotation(SJORMTable.class).tableName();

        primaryKeyFieldsLeft = SJORM.getInstance().getSJORMConnection().getPrimaryKeys(tableLeft);
        primaryKeyFieldsRight = SJORM.getInstance().getSJORMConnection().getPrimaryKeys(tableRight);
    }

    /**
     * The type of the left side of the pivot model this metadata is associated with
     * @return The type of the left side of the pivot model this metadata is associated with
     */
    public Class<? extends Model> getTypeLeft() {
        return typeLeft;
    }

    /**
     * The type of the right side of the pivot model this metadata is associated with
     * @return The type of the right side of the pivot model this metadata is associated with
     */
    public Class<? extends Model> getTypeRight() {
        return typeRight;
    }

    /**
     * The table name of the left side as it is described in the database
     * @return The table name of the left side as it is described in the database
     */
    public String getTableLeft() {
        return tableLeft;
    }

    /**
     * The table name of the right side as it is described in the database
     * @return The table name of the right side as it is described in the database
     */
    public String getTableRight() {
        return tableRight;
    }

    /**
     * The column name(s) of the left side of the primary key of the table in the database
     * @return The column name(s) of the left side of the primary key of the table in the database
     */
    public String[] getPrimaryKeyFieldsLeft() {
        return primaryKeyFieldsLeft;
    }

    /**
     * The column name(s) of the right side of the primary key of the table in the database
     * @return The column name(s) of the right side of the primary key of the table in the database
     */
    public String[] getPrimaryKeyFieldsRight() {
        return primaryKeyFieldsRight;
    }
}
