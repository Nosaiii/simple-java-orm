package main.java.com.nosaiii.sjorm.querybuilder.condition;

public interface SQLCondition {
    String build();
    Object[] getObfuscatedValues();
}