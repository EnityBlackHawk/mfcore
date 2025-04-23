package org.utfpr.mf.reflection;

import org.utfpr.mf.json.JsonSchema;
import org.utfpr.mf.json.JsonSchemaList;
import org.utfpr.mf.json.JsonType;
import org.utfpr.mf.json.Reference;
import org.utfpr.mf.orms.ORM;
import org.utfpr.mf.tools.CodeSession;
import org.utfpr.mf.tools.TemplatedString;

import java.util.*;

public class JsonSchemaToClassConverter extends CodeSession {

    private final ORM orm;
    private HashMap<String, ClassMetadata> classes = new HashMap<>();
    private String currentTitle = "";

    public JsonSchemaToClassConverter(ORM orm) {
        super("JsonSchemaToClassConverter", LastSet);
        this.orm = orm;
    }

    public ClassMetadataList convertMany(JsonSchemaList list) {

        for(var json : list) {
            convert(json, TemplatedString.capitalize(json.getTitle()));
        }

        return new ClassMetadataList(classes.values());
    }

    private FieldMetadata createObjectField(JsonSchema parent, Map.Entry<String, JsonSchema> field) {

        JsonSchema value = field.getValue();

        if(!value.getIsAbstract() && (value.getReferenceTo() == null || value.getReferenceTo().getTargetTable() == null)) {

            var firstChild = value.getProperties().entrySet().stream().findFirst();

            if(firstChild.isEmpty()) {
                throw new RuntimeException("ReferenceTo is null - All object fields must have a reference to another table\n Field: " + field.getKey());
            }

            value.setReferenceTo(
                    new Reference(
                            firstChild.get().getValue().getTable(),
                            "$auto"
                    )
            );
        }

        String innerClassName;

        if(value.getReference() != null && value.getReference()) {

            innerClassName = TemplatedString.capitalize(Objects.requireNonNull(value.getDocReferenceTo(), "Reference = true but docReference is null on field " + field.getKey() + " of " + parent));

            if(innerClassName.isEmpty()) {
                throw new RuntimeException("docReferenceTo is null - All object references must have a docReferenceTo \n Field: " + field.getKey());
            }

        } else {
            innerClassName = TemplatedString.capitalize(
                    // TODO: Check here if the parent is abstract
                    value.getIsAbstract()
                            ? Objects.requireNonNullElse(parent.getTitle(), parent.getTable()) + TemplatedString.capitalize(field.getKey())
                            : value.getReferenceTo().getTargetTable()
            );
        }

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
        switch (field.getValue().getType()) {
            case JsonType.STRING, JsonType.NULL -> {
                return new FieldMetadata(field.getKey(), "java.lang.String", isId ? List.of(orm.getIdAnnotation()) : List.of());
            }
            case JsonType.NUMBER -> {
                return new FieldMetadata(field.getKey(), "java.lang.Double", isId ? List.of(orm.getIdAnnotation()) : List.of());
            }
            case JsonType.INTEGER -> {
                return new FieldMetadata(field.getKey(), "java.lang.Integer", isId ? List.of(orm.getIdAnnotation()) : List.of());
            }
            case JsonType.BOOLEAN -> {
                return new FieldMetadata(field.getKey(), "java.lang.Boolean", isId ? List.of(orm.getIdAnnotation()) : List.of());
            }
            case JsonType.ARRAY -> {


                if (field.getValue().getItems().getType().equals(JsonType.OBJECT)) {
                    String className = TemplatedString.capitalize(field.getKey()) + "Items";
                    ClassMetadata innerClass = convert(field.getValue().getItems(), className);
                    return new FieldMetadata(field.getKey(), "java.util.List<" + className + ">", List.of());
                }

                FieldMetadata fmd = createField(Map.entry(field.getKey(), field.getValue().getItems()));
                return new FieldMetadata(field.getKey(), "java.util.List<" + fmd.getType() + ">", List.of());
            }
        }

        throw new RuntimeException("Type not defined:" + field.getValue().getType() + "\nField: " + field.getKey());
    }

    public ClassMetadata convert(JsonSchema root, String className) {

        ArrayList<FieldMetadata> fields = new ArrayList<>();
        currentTitle = root.getTitle();

        if(root.getProperties() == null) {
            ERROR("Properties is null on " + className);
            ERROR("All objects must have properties defined");
            root.setProperties(new HashMap<>());
        }

        for(var field : root.getProperties().entrySet()) {

            if( field.getValue().getReference() || field.getValue().getType().equals(JsonType.OBJECT)) {
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
