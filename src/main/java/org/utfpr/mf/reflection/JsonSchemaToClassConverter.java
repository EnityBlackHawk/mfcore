package org.utfpr.mf.reflection;

import org.utfpr.mf.json.JsonSchema;
import org.utfpr.mf.orms.ORM;

public class JsonSchemaToClassConverter {

    private final ORM orm;

    public JsonSchemaToClassConverter(ORM orm) {
        this.orm = orm;
    }

    public ClassMetadata convert(JsonSchema jsonSchema, String className) {

        return null;

    }

}
