package com.nosaiii.sjorm.querybuilder.condition;

public interface SQLCondition {
    /**
     * Builds the condition to be used in an SQL query
     * @return The condition to be used in an SQL query
     */
    String build();

    /**
     * Gets the values of the obfuscated condition that were hidden from the {@code build()} method
     * @return An array of the obfuscated values
     */
    Object[] getObfuscatedValues();
}