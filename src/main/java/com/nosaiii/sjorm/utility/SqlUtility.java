package main.java.com.nosaiii.sjorm.utility;

import java.util.ArrayList;
import java.util.List;

public class SqlUtility {
    public static String quote(String str) {
        return "`" + str + "`";
    }

    public static String quote(Iterable<? extends CharSequence> elements) {
        List<String> list = new ArrayList<>();

        for(CharSequence entry : elements) {
            list.add("`" + entry + "`");
        }

        return String.join(", ", list);
    }
}