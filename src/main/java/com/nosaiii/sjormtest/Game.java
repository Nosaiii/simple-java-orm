package main.java.com.nosaiii.sjormtest;

import main.java.com.nosaiii.sjorm.Model;
import main.java.com.nosaiii.sjorm.annotations.SJORMTable;

import java.sql.ResultSet;

@SJORMTable(tableName = "game")
public class Game extends Model {}