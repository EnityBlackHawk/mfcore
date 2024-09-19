package org.mf.langchain.util;

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

}
