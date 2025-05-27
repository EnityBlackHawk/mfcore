package org.utfpr.mf.reflection;

import org.utfpr.mf.json.*;
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

            if(value.getProperties() == null) {
                INFO("Properties is null on " + field.getKey());
                value.setProperties(new HashMap<>());
            }

            var firstChild = value.getProperties().entrySet().stream().findFirst();

            if(firstChild.isEmpty()) {
                ERROR("ReferenceTo is null - All object fields must have a reference to another table\n Field: " + field.getKey());
                return null;
            }

            value.setReferenceTo(
                    new Reference(
                            firstChild.get().getValue().getTable(),
                            "$auto"
                    )
            );
        }

        String innerClassName;

        if(value.getReference()) {

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

        if(value.getReference()) {
            return new FieldMetadata(field.getKey(), innerClass.getClassName(), List.of(orm.getReferenceAnnotation()));
        }

        return new FieldMetadata(field.getKey(), innerClass.getClassName(), List.of());
    }

    private FieldMetadata createField(ClassMetadata clazz, Map.Entry<String, JsonSchema> field) {
        boolean isId = field.getValue().getIsId() != null && field.getValue().getIsId() && !hasId(clazz);
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

                FieldMetadata fmd = createField(clazz, Map.entry(field.getKey(), field.getValue().getItems()));
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
        var meta = new ClassMetadata(className, fields);

        for(var field : root.getProperties().entrySet()) {

            if(Objects.equals(field.getValue().getType(), JsonType.ARRAY)) {
                field.getValue().setRelationshipType(RelationshipType.EMBEDDED);
            }

            if( field.getValue().getReference() || field.getValue().getType().equals(JsonType.OBJECT)) {
                var f = createObjectField(root, field);
                if(f != null) {
                    fields.add(f);
                }
                continue;
            }

            meta.getFields().add( createField(meta, field) );

        }

        classes.put(className, meta);
        return meta;

    }

    public boolean hasId(ClassMetadata clazz) {
        return clazz.getFields().stream().anyMatch( (f) -> f.getAnnotations().stream().anyMatch( (a) -> a.equals(orm.getIdAnnotation())));
    }

    public void checkAndAppend(JsonSchema schema, ClassMetadata classMetadata) {
        if(schema.getProperties() == null) {
            return;
        }
        for(var field : schema.getProperties().entrySet()) {
            if(classMetadata.getFields().stream().noneMatch(f -> Objects.equals(f.getName(), field.getKey()))) {
                if(field.getValue().getType().equals(JsonType.OBJECT)) {
                    classMetadata.getFields().add( createObjectField(schema, field));
                    continue;
                }
                classMetadata.getFields().add( createField(classMetadata, field) );
            }
        }
    }

}
