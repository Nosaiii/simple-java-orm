package main.java.com.nosaiii.sjorm.querybuilder;

public class SQLPair {
    private final String field;
    private final Object value;

    public SQLPair(String field, Object value) {
        this.field = field;
        this.value = value;
    }

    public String getField() {
        return field;
    }

    public Object getValue() {
        return value;
    }
}