package com.nosaiii.sjorm.models;

import com.nosaiii.sjorm.Model;
import com.nosaiii.sjorm.Query;
import com.nosaiii.sjorm.annotations.SJORMTable;

import java.sql.ResultSet;

@SJORMTable(tableName = "dummy_primary")
public class DummyModelPrimary extends Model {
    public DummyModelPrimary(ResultSet rs) {
        super(rs);
    }

    public DummyModelPrimary() {
        super();
    }

    public Query<DummyModelSecondary> getSecondaries() {
        return hasMany(DummyModelSecondary.class);
    }
}