package main.java.com.nosaiii.sjorm;

public class ForeignKeyReference {
    private final String table;
    private final String column;

    public ForeignKeyReference(String table, String column) {
        this.table = table;
        this.column = column;
    }

    /**
     * The name of the table of the foreign key reference
     * @return The name of the table of the foreign key reference
     */
    public String getTable() {
        return table;
    }

    /**
     * The name of the column of the foreign key reference
     * @return The name of the column of the foreign key reference
     */
    public String getColumn() {
        return column;
    }
}