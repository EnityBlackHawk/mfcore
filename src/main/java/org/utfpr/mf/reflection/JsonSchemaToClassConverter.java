package org.utfpr.mf.reflection;

import org.utfpr.mf.json.JsonSchema;
import org.utfpr.mf.json.JsonType;
import org.utfpr.mf.orms.ORM;

import java.util.*;

public class JsonSchemaToClassConverter {

    private final ORM orm;
    private HashMap<String, ClassMetadata> classes = new HashMap<>();

    public JsonSchemaToClassConverter(ORM orm) {
        this.orm = orm;
    }

    private FieldMetadata createObjectField( Map.Entry<String, JsonSchema> field) {

        JsonSchema value = field.getValue();

        if(value.getReferenceTo() == null || value.getReferenceTo().getTargetTable() == null) {
            throw new RuntimeException("Reference to is null");
        }

        String innerClassName = value.getReferenceTo().getTargetTable();
        ClassMetadata innerClass;
        if(classes.containsKey(innerClassName)) {
            checkAndAppend(value, classes.get(innerClassName));
            innerClass = classes.get(innerClassName);
        }
        else {
            innerClass = convert(value, innerClassName);
            classes.put(innerClassName, innerClass);
        }

        if(value.getReference() != null && value.getReference()) {
            return new FieldMetadata(field.getKey(), innerClass.getClassName(), List.of(orm.getReferenceAnnotation()));
        }

        return new FieldMetadata(field.getKey(), innerClass.getClassName(), List.of());
    }

    private FieldMetadata createField(Map.Entry<String, JsonSchema> field) {
        boolean isId = field.getValue().getIsId() != null && field.getValue().getIsId();
        if(field.getValue().getType() == JsonType.STRING) {
            return new FieldMetadata(field.getKey(), "java.lang.String", isId ? List.of(orm.getIdAnnotation()) : List.of());

        }

        if(field.getValue().getType() == JsonType.NUMBER) {
            return  new FieldMetadata(field.getKey(), "java.lang.Double", isId ? List.of(orm.getIdAnnotation()) : List.of());
        }

        if(field.getValue().getType() == JsonType.INTEGER) {
            return new FieldMetadata(field.getKey(), "java.lang.Integer", isId ? List.of(orm.getIdAnnotation()) : List.of());

        }

        if(field.getValue().getType() == JsonType.ARRAY) {
            return null;
        }

        throw new RuntimeException("Type not defined");
    }

    public ClassMetadata convert(JsonSchema root, String className) {

        ArrayList<FieldMetadata> fields = new ArrayList<>();

        for(var field : root.getProperties().entrySet()) {

            if(field.getValue().getType() == JsonType.OBJECT) {
                fields.add(createObjectField(field));
                continue;
            }

            fields.add(createField(field));

        }

        return new ClassMetadata(className, fields);

    }

    public void checkAndAppend(JsonSchema schema, ClassMetadata classMetadata) {
        for(var field : schema.getProperties().entrySet()) {
            if(classMetadata.getFields().stream().noneMatch(f -> Objects.equals(f.getName(), field.getKey()))) {
                classMetadata.getFields().add( createField( field) );
            }
        }
    }

}
