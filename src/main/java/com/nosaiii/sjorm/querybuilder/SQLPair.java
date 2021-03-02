package com.nosaiii.sjorm.querybuilder;

public class SQLPair {
    private final String field;
    private final Object value;

    public SQLPair(String field, Object value) {
        this.field = field;
        this.value = value;
    }

    /**
     * The field of the pair
     * @return The field of the pair
     */
    public String getField() {
        return field;
    }

    /**
     * The value of the pair
     * @return The value of the pair
     */
    public Object getValue() {
        return value;
    }
}