package com.nosaiii.sjorm.querybuilder;

import com.nosaiii.sjorm.querybuilder.condition.SQLCondition;
import com.nosaiii.sjorm.utility.SQLUtility;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class QueryBuilder {
    private final Connection connection;
    private final StringBuilder builder;

    private final List<Object> parameters;

    public QueryBuilder(Connection connection) {
        this.connection = connection;
        builder = new StringBuilder();

        parameters = new ArrayList<>();
    }

    /**
     * Performs a result-given query using the built query and returns a {@link ResultSet} that is not automatically being disposed after execution
     * @return A {@link ResultSet} containing data from the executed query
     */
    public ResultSet executeQuery() {
        try {
            return buildStatement().executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Performs an UPDATE query using the built query and returns the amount of affected rows
     * @return The amount of affected rows by the performed UPDATE query
     */
    public int executeUpdate() {
        try (PreparedStatement statement = buildStatement()) {
            return statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0;
    }

    /**
     * Builds the {@link PreparedStatement} object by building the query and binding parameters to properties
     * @return A {@link PreparedStatement} object with bound properties
     * @throws SQLException Thrown when the {@code prepared statement} could not be created
     */
    private PreparedStatement buildStatement() throws SQLException {
        PreparedStatement statement = connection.prepareStatement(builder.toString());

        for (int i = 1; i <= parameters.size(); i++) {
            statement.setObject(i, parameters.get(i - 1));
        }

        return statement;
    }

    /**
     * (UNSAFE!) Appends plain SQL to the query
     * @param sql The plain SQL to append to the query
     * @return This instance of the {@link QueryBuilder} to append statements to
     */
    public QueryBuilder sql(String sql) {
        builder.append(sql);
        return this;
    }

    /**
     * Appends a SELECT statement to the query
     * @param columns (Optional) columns to select. If no columns were given, all columns will be selected
     * @return This instance of the {@link QueryBuilder} to append statements to
     */
    public QueryBuilder select(String... columns) {
        builder.append("SELECT ");

        if (columns.length == 0) {
            builder.append("*");
        } else {
            builder.append(SQLUtility.quote(Arrays.asList(columns)));
        }
        builder.append(" ");

        return this;
    }

    /**
     * Appends a FROM statement to the query
     * @param table The name of the table to select from
     * @return This instance of the {@link QueryBuilder} to append statements to
     */
    public QueryBuilder from(String table) {
        builder.append("FROM ").append(SQLUtility.quote(table)).append(" ");
        return this;
    }

    /**
     * Appends a FROM statement to the query
     * @param prefixTable The name of the prefix table to select from
     * @param table The name of the table to select from
     * @return This instance of the {@link QueryBuilder} to append statements to
     */
    public QueryBuilder from(String prefixTable, String table) {
        builder.append("FROM ").append(SQLUtility.quote(prefixTable)).append(".").append(SQLUtility.quote(table)).append(" ");
        return this;
    }

    /**
     * Appends a WHERE statement to the query
     * @param condition A {@link SQLCondition} instance describing how to construct the condition in the WHERE statement
     * @return This instance of the {@link QueryBuilder} to append statements to
     */
    public QueryBuilder where(SQLCondition condition) {
        builder.append("WHERE ").append(condition.build()).append(" ");

        parameters.addAll(Arrays.asList(condition.getObfuscatedValues()));

        return this;
    }

    /**
     * Appends a condition to a previously appended WHERE clause using the AND keyword
     * @param condition A {@link SQLCondition} instance describing how to construct the condition in the WHERE statement
     * @return This instance of the {@link QueryBuilder} to append statements to
     */
    public QueryBuilder and(SQLCondition condition) {
        builder.append("AND ").append(condition.build()).append(" ");

        parameters.addAll(Arrays.asList(condition.getObfuscatedValues()));

        return this;
    }

    /**
     * Appends a condition to a previously appended WHERE clause using the OR keyword
     * @param condition A {@link SQLCondition} instance describing how to construct the condition in the WHERE statement
     * @return This instance of the {@link QueryBuilder} to append statements to
     */
    public QueryBuilder or(SQLCondition condition) {
        builder.append("OR ").append(condition.build()).append(" ");

        parameters.addAll(Arrays.asList(condition.getObfuscatedValues()));

        return this;
    }

    /**
     * Appends a JOIN (inner, left, right) statement to the query
     * @param sqlJoin The type of join to use
     * @param targetTable The target table to join its data from
     * @param condition A {@link SQLCondition} instance describing how to construct the match between tables in the JOIN statement
     * @return This instance of the {@link QueryBuilder} to append statements to
     */
    public QueryBuilder join(SQLJoin sqlJoin, final String targetTable, SQLCondition condition) {
        builder.append(" ").append(sqlJoin.getString()).append(" ");
        builder.append(SQLUtility.quote(targetTable));
        builder.append(" ON ");
        builder.append(condition.build());

        parameters.addAll(Arrays.asList(condition.getObfuscatedValues()));

        return this;
    }

    /**
     * Appends an ORDER BY statement to the query
     * @param column The name of the base column to order by
     * @param columns (Optional) column names to hierarchically order by
     * @return This instance of the {@link QueryBuilder} to append statements to
     */
    public QueryBuilder orderBy(String column, String... columns) {
        builder.append("ORDER BY ").append(SQLUtility.quote(column)).append(" ");

        if (columns.length > 0) {
            builder.append(SQLUtility.quote(Arrays.asList(columns)));
        }
        builder.append(" ");

        return this;
    }

    /**
     * Appends a GROUP BY statement to the query
     * @param column The name of the base column to group by
     * @param columns (Optional) column names to hierarchically group by
     * @return This instance of the {@link QueryBuilder} to append statements to
     */
    public QueryBuilder groupBy(String column, String... columns) {
        builder.append("GROUP BY ").append(SQLUtility.quote(column)).append(" ");

        if (columns.length > 0) {
            builder.append(SQLUtility.quote(Arrays.asList(columns)));
        }
        builder.append(" ");

        return this;
    }

    /**
     * Appends a LIMIT statement to the query
     * @param limit The limited amount of rows to retrieve
     * @return This instance of the {@link QueryBuilder} to append statements to
     */
    public QueryBuilder limit(int limit) {
        return limit(limit, 0);
    }

    /**
     * Appends a LIMIT statement to the query (with offset)
     * @param limit The limited amount of rows to retrieve
     * @param offset The offset, from the start, to limit the rows on
     * @return This instance of the {@link QueryBuilder} to append statements to
     */
    public QueryBuilder limit(int limit, int offset) {
        builder.append("LIMIT ").append(offset).append(", ").append(limit).append(" ");
        return this;
    }

    /**
     * Appends an INSERT INTO statement to the query
     * @param table The name of the table to insert data in
     * @param columns The name of the columns to insert data in. If left empty, all columns in the table will be filled
     * @return This instance of the {@link QueryBuilder} to append statements to
     */
    public QueryBuilder insertInto(String table, String... columns) {
        builder.append("INSERT INTO ").append(SQLUtility.quote(table));

        if(columns.length > 0) {
            builder.append("(").append(SQLUtility.quote(Arrays.asList(columns))).append(")");
        }

        builder.append(" ");

        return this;
    }

    /**
     * Appends an INSERT INTO statement to the query
     * @param table The name of the table to insert data in
     * @param columns A distinct set of the columns to insert data in
     * @return This instance of the {@link QueryBuilder} to append statements to
     */
    public QueryBuilder insertInto(String table, Set<String> columns) {
        return insertInto(table, columns.toArray(new String[0]));
    }

    /**
     * Appends a list of values to the query, used for the INSERT INTO statement
     * @param values A collection of values to insert
     * @return This instance of the {@link QueryBuilder} to append statements to
     */
    public QueryBuilder values(Collection<Object> values) {
        builder.append("VALUES (");

        if(values.size() > 0) {
            String[] parameterCharacters = new String[values.size()];
            Arrays.fill(parameterCharacters, "?");
            builder.append(String.join(", ", parameterCharacters));
        }

        builder.append(")");

        parameters.addAll(values);

        return this;
    }

    /**
     * Appends a list of values to the query, used for the INSERT INTO statement, using a subquery as values
     * @param queryBuilder A {@link QueryBuilder} object used to subquery a collection
     * @return This instance of the {@link QueryBuilder} to append statements to
     */
    public QueryBuilder values(QueryBuilder queryBuilder) {
        builder.append("VALUES (");
        builder.append(queryBuilder.builder.toString());
        builder.append(")");

        parameters.add(queryBuilder.parameters);

        return this;
    }

    /**
     * Appends an UPDATE statement to the query
     * @param table The name of the table to update its values for
     * @param pairs Pairs of fields and keys (in the form of {@link SQLPair} objects) to be updated
     * @return This instance of the {@link QueryBuilder} to append statements to
     */
    public QueryBuilder update(String table, SQLPair... pairs) {
        builder.append("UPDATE ").append(SQLUtility.quote(table)).append(" SET ");

        List<String> setStatements = new ArrayList<>();
        for(SQLPair pair : pairs) {
            setStatements.add(SQLUtility.quote(pair.getField()) + " = ?");
        }
        builder.append(String.join(", ", setStatements));

        builder.append(" ");

        parameters.addAll(Arrays.stream(pairs).map(SQLPair::getValue).collect(Collectors.toList()));

        return this;
    }
}