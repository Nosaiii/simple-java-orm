package main.java.com.nosaiii.sjorm;

import main.java.com.nosaiii.sjorm.annotations.SJORMTable;

public class ModelMetadata {
    private final Class<? extends Model> type;
    private final String table;
    private final String[] primaryKeyFields;

    public ModelMetadata(Class<? extends Model> type) {
        this.type = type;
        this.table = type.getAnnotation(SJORMTable.class).tableName();
        primaryKeyFields = SJORM.getInstance().getSJORMConnection().getPrimaryKeys(table);
    }

    public Class<? extends Model> getType() {
        return type;
    }

    public String getTable() {
        return table;
    }

    public String[] getPrimaryKeyFields() {
        return primaryKeyFields;
    }
}