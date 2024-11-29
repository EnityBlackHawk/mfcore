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

    public static String camelCaseToSnakeCase(String camelCase) {
        return camelCase.replaceAll("([a-z])([A-Z]+)", "$1_$2").toLowerCase();
    }

}
