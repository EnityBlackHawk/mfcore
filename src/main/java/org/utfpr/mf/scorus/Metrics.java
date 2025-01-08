package org.utfpr.mf.scorus;

import org.utfpr.mf.json.JsonSchema;
import org.utfpr.mf.json.JsonSchemaList;

import java.util.ArrayList;
import java.util.List;

public class Metrics {

    public static int colExistence(JsonSchemaList schema, String collectionName) {
        return schema.stream().anyMatch(s -> s.getTitle().equals(collectionName)) ? 1 : 0;
    }

    public static int colDepth(JsonSchema schema) {
        var props = schema.getProperties();
        if (props != null && isObject(schema)) {
            ArrayList<Integer> accs = new ArrayList<>();
            for(var prop : props.values().stream().filter(Metrics::isObject).toList()) {
                    accs.add(colDepth(prop) + 1);
            }
            return accs.stream().max(Integer::compareTo).orElse(0);
        }
        return 0;
    }



    private static boolean isObject(JsonSchema schema) {
        return schema.getType().equals("object") || (schema.getType().equals("array") && schema.getItems().getType().equals("object"));
    }
}
