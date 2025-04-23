package org.utfpr.mf.tools;

import org.springframework.data.util.Pair;

import java.util.Map;

public class TemplatedString {

    private final String template;

    public TemplatedString(String template) {
        this.template = template;
    }

    @SafeVarargs
    public final String render(Pair<String, String>... values) {
        String result = template;
        for (var x : values) {
            result = result.replaceAll("\\{\\{" + x.getFirst() + "}}", x.getSecond());
        }
        return result;
    }

    public static String snakeCaseToCamelCase(String snakeCase) {
    StringBuilder result = new StringBuilder();
    boolean toUpperCase = false;
    for (char c : snakeCase.toCharArray()) {
        if (c == '_') {
            toUpperCase = true;
        } else {
            result.append(toUpperCase ? Character.toUpperCase(c) : c);
            toUpperCase = false;
        }
    }
    return result.toString();
}

    public static String camelCaseToSnakeCase(String camelCase) {
        return camelCase.replaceAll("([a-z])([A-Z]+)", "$1_$2").toLowerCase();
    }

    public static String capitalize(String s) {
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    public static String toSingular(String name) {
        if(name.endsWith("ies")) {
            return name.substring(0, name.length() - 3) + "y";
        }

        if(name.endsWith("s")) {
            return name.substring(0, name.length() - 1);
        }

        return name;
    }

}
