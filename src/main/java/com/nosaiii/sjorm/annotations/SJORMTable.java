package com.nosaiii.sjorm.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface SJORMTable {
    /**
     * The name of the table to associate the metadata of the model with
     * @return The name of the table to associate the metadata of the model with
     */
    String tableName();
}