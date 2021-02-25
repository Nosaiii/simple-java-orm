package main.java.com.nosaiii.sjormtest;

import main.java.com.nosaiii.sjorm.Model;
import main.java.com.nosaiii.sjorm.annotations.SJORMTable;

import java.sql.ResultSet;

@SJORMTable(tableName = "team")
public class Team extends Model {
    public Team(ResultSet resultSet) {
        super(resultSet);
    }

    public Team() {
        super();
    }
}
