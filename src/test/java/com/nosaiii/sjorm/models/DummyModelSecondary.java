package com.nosaiii.sjorm.models;

import com.nosaiii.sjorm.Model;
import com.nosaiii.sjorm.annotations.SJORMTable;

import java.sql.ResultSet;

@SJORMTable(tableName = "dummy_secondary")
public class DummyModelSecondary extends Model {
    public DummyModelSecondary(ResultSet rs) {
        super(rs);
    }

    public DummyModelSecondary() {
        super();
    }
}