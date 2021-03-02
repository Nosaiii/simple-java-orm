package com.nosaiii.sjorm.querybuilder;

public enum SQLJoin {
    INNER_JOIN("INNER JOIN"),
    LEFT_JOIN("LEFT JOIN"),
    RIGHT_JOIN("RIGHT JOIN");

    private final String join;

    SQLJoin(String join) {
        this.join = join;
    }

    /**
     * The SQL statement of the join-statement as how it would look like in an SQL query
     * @return
     */
    public String getString() {
        return join;
    }
}