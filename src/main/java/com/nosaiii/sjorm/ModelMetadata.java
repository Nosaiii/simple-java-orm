package main.java.com.nosaiii.sjorm;

public class ModelMetadata {
    private final String table;
    private final String[] primaryKeyFields;

    public ModelMetadata(String table, String[] primaryKeyFields) {
        this.table = table;
        this.primaryKeyFields = primaryKeyFields;
    }

    public String getTable() {
        return table;
    }

    public String[] getPrimaryKeyFields() {
        return primaryKeyFields;
    }
}