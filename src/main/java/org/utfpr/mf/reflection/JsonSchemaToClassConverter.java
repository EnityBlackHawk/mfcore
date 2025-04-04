package org.utfpr.mf.reflection;

import dev.ai4j.openai4j.Json;
import org.utfpr.mf.json.JsonSchema;
import org.utfpr.mf.json.JsonSchemaList;
import org.utfpr.mf.json.JsonType;
import org.utfpr.mf.json.Reference;
import org.utfpr.mf.orms.ORM;
import org.utfpr.mf.tools.TemplatedString;

import java.util.*;

public class JsonSchemaToClassConverter {

    private final ORM orm;
    private HashMap<String, ClassMetadata> classes = new HashMap<>();
    private String currentTitle = "";

    public JsonSchemaToClassConverter(ORM orm) {
        this.orm = orm;
    }

    public ClassMetadataList convertMany(JsonSchemaList list) {

        for(var json : list) {
            convert(json, json.getTitle());
        }

        return new ClassMetadataList(classes.values());
    }

    private FieldMetadata createObjectField(JsonSchema parent, Map.Entry<String, JsonSchema> field) {

        JsonSchema value = field.getValue();


        if(!value.getIsAbstract() && (value.getReferenceTo() == null || value.getReferenceTo().getTargetTable() == null)) {

            var firstChild = value.getProperties().entrySet().stream().findFirst();

            if(firstChild.isEmpty()) {
                throw new RuntimeException("Reference to is null - All object fields must have a reference to another table\n Field: " + field.getKey());
            }

            value.setReferenceTo(
                    new Reference(
                            firstChild.get().getValue().getTable(),
                            "$auto"
                    )
            );
        }

        String innerClassName = TemplatedString.capitalize(
                // TODO: Check here if the parent is abstract
                !value.getIsAbstract()
                        ? value.getReferenceTo().getTargetTable()
                        : Objects.requireNonNullElse(parent.getTitle(), parent.getTable()) + TemplatedString.capitalize(field.getKey())
        );
        ClassMetadata innerClass;
        if(classes.containsKey(innerClassName)) {
            checkAndAppend(value, classes.get(innerClassName));
            innerClass = classes.get(innerClassName);
        }
        else {
            innerClass = convert(value, innerClassName);
        }

        if(value.getReference() != null && value.getReference()) {
            return new FieldMetadata(field.getKey(), innerClass.getClassName(), List.of(orm.getReferenceAnnotation()));
        }

        return new FieldMetadata(field.getKey(), innerClass.getClassName(), List.of());
    }

    private FieldMetadata createField(Map.Entry<String, JsonSchema> field) {
        boolean isId = field.getValue().getIsId() != null && field.getValue().getIsId();
        if(field.getValue().getType() == JsonType.STRING || field.getValue().getType() == JsonType.NULL) {
            return new FieldMetadata(field.getKey(), "java.lang.String", isId ? List.of(orm.getIdAnnotation()) : List.of());

        }

        if(field.getValue().getType() == JsonType.NUMBER) {
            return  new FieldMetadata(field.getKey(), "java.lang.Double", isId ? List.of(orm.getIdAnnotation()) : List.of());
        }

        if(field.getValue().getType() == JsonType.INTEGER) {
            return new FieldMetadata(field.getKey(), "java.lang.Integer", isId ? List.of(orm.getIdAnnotation()) : List.of());

        }

        if (field.getValue().getType() == JsonType.BOOLEAN) {
            return new FieldMetadata(field.getKey(), "java.lang.Boolean", isId ? List.of(orm.getIdAnnotation()) : List.of());
        }

        if(field.getValue().getType() == JsonType.ARRAY) {


            if(field.getValue().getItems().getType() == JsonType.OBJECT) {
                String className = TemplatedString.capitalize(field.getKey()) + "Items";
                ClassMetadata innerClass = convert(field.getValue().getItems(), className);
                return new FieldMetadata(field.getKey(), "java.util.List<" + className + ">", List.of());
            }

            FieldMetadata fmd = createField(Map.entry(field.getKey(), field.getValue().getItems()));
            return new FieldMetadata(field.getKey(), "java.util.List<" + fmd.getType() + ">", List.of());
        }

        throw new RuntimeException("Type not defined");
    }

    public ClassMetadata convert(JsonSchema root, String className) {

        ArrayList<FieldMetadata> fields = new ArrayList<>();
        currentTitle = root.getTitle();

        for(var field : root.getProperties().entrySet()) {

            if(field.getValue().getType() == JsonType.OBJECT) {
                fields.add(createObjectField(root, field));
                continue;
            }

            fields.add(createField(field));

        }
        var meta = new ClassMetadata(className, fields);
        classes.put(className, meta);
        return meta;

    }

    public void checkAndAppend(JsonSchema schema, ClassMetadata classMetadata) {
        if(schema.getProperties() == null) {
            return;
        }
        for(var field : schema.getProperties().entrySet()) {
            if(classMetadata.getFields().stream().noneMatch(f -> Objects.equals(f.getName(), field.getKey()))) {
                classMetadata.getFields().add( createField( field) );
            }
        }
    }

}
