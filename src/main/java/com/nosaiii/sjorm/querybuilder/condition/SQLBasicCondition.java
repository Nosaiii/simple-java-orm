package com.nosaiii.sjorm.querybuilder.condition;

import com.nosaiii.sjorm.querybuilder.SQLPair;
import com.nosaiii.sjorm.utility.SQLUtility;

public class SQLBasicCondition implements SQLCondition {
    private final SQLPair pair;
    private final SQLConditionType conditionType;

    public SQLBasicCondition(String field, SQLConditionType conditionType, Object value) {
        this.pair = new SQLPair(field, value);
        this.conditionType = conditionType;
    }

    @Override
    public String build() {
        String conditionalCharacters = "";

        switch(conditionType) {
            case EQUALS: conditionalCharacters = "="; break;
            case DOES_NOT_EQUAL: conditionalCharacters = "!="; break;
            case GREATER_THAN: conditionalCharacters = ">"; break;
            case LESS_THAN: conditionalCharacters = "<"; break;
            case GREATER_EQUAL_THAN: conditionalCharacters = ">="; break;
            case LESS_EQUAL_THAN: conditionalCharacters = "<="; break;
            case BETWEEN: conditionalCharacters = "BETWEEN"; break;
            case LIKE: conditionalCharacters = "LIKE"; break;
        }

        return SQLUtility.quote(pair.getField()) + " " + conditionalCharacters + " ?";
    }

    @Override
    public Object[] getObfuscatedValues() {
        return new Object[] { pair.getValue() };
    }
}