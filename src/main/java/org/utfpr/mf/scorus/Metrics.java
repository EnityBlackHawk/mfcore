package org.utfpr.mf.scorus;

import org.utfpr.mf.json.JsonSchema;
import org.utfpr.mf.json.JsonSchemaList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class Metrics {

    public static int colExistence(JsonSchemaList schema, String collectionName) {
        return schema.stream().anyMatch(s -> s.getTitle().equals(collectionName)) ? 1 : 0;
    }

    public static int colDepth(JsonSchema doc) {
        if(!isObject(doc)) {
            return 0;
        }

        HashMap<String, JsonSchema> props;
        if (doc.getType().equals("object")) {
            props = doc.getProperties();
        } else {
            if(doc.getItems().getType().equals("object")) {
                props = doc.getItems().getProperties();
            }
            else {
                return 0;
            }
        }

        if(props == null) {
            return -1;
        }

        ArrayList<Integer> accs = new ArrayList<>();
        for(var prop : props.values().stream().filter(Metrics::isObject).toList()) {
            accs.add(colDepth(prop) + 1);
        }
        return accs.stream().max(Integer::compareTo).orElse(0);


    }

    public static int globalDepth(JsonSchemaList schemaList) {
        return schemaList.stream().mapToInt(Metrics::colDepth).max().orElse(0);
    }

    public static int docWidth(JsonSchema doc) {
        var props = doc.getProperties();
        if(props == null) {
            return 0;
        }
        int atc = props.values().stream().filter((x) -> !isObjectOrArray(x)).mapToInt(a -> 1).sum();
        int emb = props.values().stream().filter((x) -> x.getType().equals("object")).mapToInt(b -> 2).sum();
        int arE = props.values().stream().filter((x) -> isArray(x) && isItemsObject(x)).mapToInt(c -> 3).sum();
        int arA = props.values().stream().filter((x) -> isArray(x) && !isItemsObject(x)).mapToInt(d -> 1).sum();
        return atc + emb + arE + arA;
    }

    public static int refLoad(JsonSchemaList schema, String docName) {
        return (int) schema.stream()
        .map(JsonSchema::getProperties)
        .filter(Objects::nonNull)
        .flatMap(props -> props.values().stream())
        .filter(prop -> Objects.requireNonNullElse(prop.getReference(), false) && prop.getDocReferenceTo().equalsIgnoreCase(docName))
        .count();
    }

    private static boolean isItemsObject(JsonSchema schema) {
        return schema.getItems().getType().equals("object");
    }

    private static boolean isArray(JsonSchema schema) {
        return schema.getType().equals("array");
    }

    private static boolean isObjectOrArray(JsonSchema schema) {
        return schema.getType().equals("object") || schema.getType().equals("array");
    }

    private static boolean isObject(JsonSchema schema) {
        return schema.getType().equals("object") || (schema.getType().equals("array") && schema.getItems().getType().equals("object"));
    }
}
