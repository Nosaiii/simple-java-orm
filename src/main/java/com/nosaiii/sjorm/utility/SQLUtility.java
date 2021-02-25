package main.java.com.nosaiii.sjorm.utility;

import java.util.ArrayList;
import java.util.List;

public class SQLUtility {
    /**
     * Prefixes and suffixes the given string with quotes (`)
     * @param str The string to surround with quotes
     * @return The prefixes and suffixed string with quotes
     */
    public static String quote(String str) {
        return "`" + str + "`";
    }

    /**
     * Prefixes and suffixes all entries from the given collection with quotes (`) and seperates them by a comma (,)
     * @param elements The collection of elements to surround with quotes
     * @return The concancenated string with all entries quoted and seperated by a comma
     */
    public static String quote(Iterable<? extends CharSequence> elements) {
        List<String> list = new ArrayList<>();

        for(CharSequence entry : elements) {
            list.add("`" + entry + "`");
        }

        return String.join(", ", list);
    }
}