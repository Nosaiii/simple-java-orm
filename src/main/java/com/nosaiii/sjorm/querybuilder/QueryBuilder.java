package main.java.com.nosaiii.sjorm.querybuilder;

import main.java.com.nosaiii.sjorm.querybuilder.condition.SQLCondition;
import main.java.com.nosaiii.sjorm.utility.SqlUtility;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
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

    public ResultSet executeQuery() {
        try (PreparedStatement statement = buildStatement()) {
            return statement.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public int executeUpdate() {
        try (PreparedStatement statement = buildStatement()) {
            return statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0;
    }

    private PreparedStatement buildStatement() throws SQLException {
        PreparedStatement statement = connection.prepareStatement(builder.toString());

        for (int i = 1; i <= parameters.size(); i++) {
            statement.setObject(i, parameters.get(i - 1));
        }

        return statement;
    }

    public QueryBuilder select(String... columns) {
        builder.append("SELECT ");

        if (columns.length == 0) {
            builder.append("*");
        } else {
            builder.append(SqlUtility.quote(Arrays.asList(columns)));
        }
        builder.append(" ");

        return this;
    }

    public QueryBuilder from(String table) {
        builder.append("FROM ").append(SqlUtility.quote(table)).append(" ");
        return this;
    }

    public QueryBuilder where(SQLCondition condition) {
        builder.append("WHERE ").append(condition.build()).append(" ");

        parameters.addAll(Arrays.asList(condition.getObfuscatedValues()));

        return this;
    }

    public QueryBuilder and(SQLCondition condition) {
        builder.append("AND ");
        return where(condition);
    }

    public QueryBuilder or(SQLCondition condition) {
        builder.append("OR ");
        return where(condition);
    }

    public QueryBuilder join(SQLJoin sqlJoin, final String targetTable, SQLCondition condition) {
        builder.append(" ").append(sqlJoin.getString()).append(" ");
        builder.append(SqlUtility.quote(targetTable));
        builder.append(" ON ");
        builder.append(condition.build());

        parameters.addAll(Arrays.asList(condition.getObfuscatedValues()));

        return this;
    }

    public QueryBuilder orderBy(String column, String... columns) {
        builder.append("ORDER BY ").append(SqlUtility.quote(column)).append(" ");

        if (columns.length > 0) {
            builder.append(SqlUtility.quote(Arrays.asList(columns)));
        }
        builder.append(" ");

        return this;
    }

    public QueryBuilder groupBy(String column, String... columns) {
        builder.append("GROUP BY ").append(SqlUtility.quote(column)).append(" ");

        if (columns.length > 0) {
            builder.append(SqlUtility.quote(Arrays.asList(columns)));
        }
        builder.append(" ");

        return this;
    }

    public QueryBuilder limit(int limit) {
        return limit(limit, 0);
    }

    public QueryBuilder limit(int limit, int offset) {
        builder.append("LIMIT ").append(offset).append(", ").append(limit).append(" ");
        return this;
    }

    public QueryBuilder insertInto(String table, String... columns) {
        builder.append("INSERT INTO ").append(SqlUtility.quote(table));

        if(columns.length > 0) {
            builder.append("(").append(SqlUtility.quote(Arrays.asList(columns))).append(")");
        }

        builder.append(" ");

        return this;
    }

    public QueryBuilder insertInto(String table, Set<String> columns) {
        return insertInto(table, columns.toArray(new String[0]));
    }

    public QueryBuilder values(Collection<Object> values) {
        builder.append("VALUES (");

        if(values.size() > 0) {
            String[] parameterCharacters = new String[values.size()];
            Arrays.fill(parameterCharacters, "?");
            builder.append(String.join(", ", parameterCharacters));
        }

        builder.append(")");

        parameters.addAll(Collections.singletonList(values));

        return this;
    }

    public QueryBuilder values(QueryBuilder queryBuilder) {
        builder.append("VALUES (");
        builder.append(queryBuilder.builder.toString());
        builder.append(")");

        parameters.add(queryBuilder.parameters);

        return this;
    }

    public QueryBuilder update(String table, SQLPair... pairs) {
        builder.append("UPDATE ").append(SqlUtility.quote(table)).append(" SET ");

        List<String> setStatements = new ArrayList<>();
        for(SQLPair pair : pairs) {
            setStatements.add(SqlUtility.quote(pair.getField()) + " = ?");
        }
        builder.append(String.join(", ", setStatements));

        builder.append(" ");

        parameters.addAll(Arrays.stream(pairs).map(SQLPair::getValue).collect(Collectors.toList()));

        return this;
    }
}