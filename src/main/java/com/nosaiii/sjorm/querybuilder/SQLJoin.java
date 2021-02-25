package main.java.com.nosaiii.sjorm.querybuilder;

public enum SQLJoin {
    INNER_JOIN("INNER JOIN"),
    LEFT_JOIN("LEFT JOIN"),
    RIGHT_JOIN("RIGHT JOIN");

    private final String join;

    SQLJoin(String join) {
        this.join = join;
    }

    public String getString() {
        return join;
    }
}