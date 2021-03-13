package com.nosaiii.sjorm.models;

import com.nosaiii.sjorm.Model;
import com.nosaiii.sjorm.annotations.SJORMTable;

import java.sql.ResultSet;

@SJORMTable(tableName = "dummy")
public class DummyModel extends Model {
    public DummyModel(ResultSet rs) {
        super(rs);
    }

    public DummyModel() {
        super();
    }
}