package org.utfpr.mf.reflection;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.expr.BooleanLiteralExpr;
import com.github.javaparser.ast.expr.ClassExpr;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.Type;
import com.google.gson.Gson;
import org.jetbrains.annotations.Nullable;
import org.springframework.data.mongodb.core.mapping.Document;
import org.utfpr.mf.annotation.FromRDB;
import org.utfpr.mf.json.JsonSchema;
import org.utfpr.mf.json.JsonSchemaList;

import java.util.*;

public class MfClassGenerator {

    private final String classMetadaString;
    private final String model;
    private final ClassMetadataList list;
    private final JsonSchemaList schemas;

    public MfClassGenerator(String classMetadataString, @Nullable String model) {
        this.classMetadaString = classMetadataString;
        this.model = model;
        Gson gson = new Gson();
        this.list = gson.fromJson(classMetadaString, ClassMetadataList.class);
        this.schemas = gson.fromJson(model, JsonSchemaList.class);
    }

    public HashMap<String, String> generate() throws ClassNotFoundException {

        HashMap<String, String> classes = new HashMap<>();

        for(JsonSchema schema : schemas) {

            ClassMetadata cm = list.stream().filter(c -> c.getClassName().equals(schema.getTitle())).findFirst().orElse(null);
            if(cm == null) {
                throw new ClassNotFoundException("Class not found: " + schema.getTitle());
            }


            List<CompilationUnit> units = createClass(cm,  schema);
            for(CompilationUnit unit : units) {
                classes.put(unit.getType(0).getNameAsString(), unit.toString());
            }
        }
        return classes;
    }

    private List<CompilationUnit> createClass(String name, JsonSchema schema) {

        var cm = list.stream().filter(c -> c.getClassName().equals(name)).findFirst().orElse(null);
        if(cm == null) {
            throw new RuntimeException("Class not found: " + name);
        }
        return createClass(cm, schema);

    }

    private List<CompilationUnit> createClass(ClassMetadata cm, JsonSchema schema) {

        List<CompilationUnit> units = new ArrayList<>();

        CompilationUnit unit = new CompilationUnit();

        var classDec = unit.addClass(cm.getClassName(), Modifier.Keyword.PUBLIC);
        classDec.addAnnotation(Document.class);
        classDec.addAnnotation("lombok.Data");

        for(FieldMetadata fmd : cm.getFields()) {

            String className = fmd.getType();
            if(className.contains("<")) {
                className = className.substring(0, className.indexOf("<"));
            }
            unit.addImport(className);
            var fieldDec = classDec.addPrivateField(fmd.getType(), fmd.getName());

            for(String ann : fmd.getAnnotations()) {
                fieldDec.addAnnotation(ann);
            }

            // TODO Tratar classes duplicadas
            if(!className.contains(".")) {
                var nestedUnit = createClass(className, schema.getProperties().get(fmd.getName()));
                units.addAll(nestedUnit);
            }

            JsonSchema sf = schema.getProperties().get(fmd.getName());

            Type classType = new ClassOrInterfaceType(null, className);

            Object isReference = sf.getReference();
            String type = sf.getType().toString();
            NormalAnnotationExpr fromRDB = fieldDec.addAndGetAnnotation(FromRDB.class)
                    .addPair("type", Objects.equals(type, "object") ? className : type )
                    .addPair("typeClass", new ClassExpr(classType))
                    .addPair("column", sf.getColumn())
                    .addPair("table", sf.getTable())
                    .addPair("isReference", new BooleanLiteralExpr(isReference != null && Boolean.parseBoolean(isReference.toString())));

            if(sf.getReferenceTo() != null) {
                fromRDB.addPair("targetTable", sf.getReferenceTo().getTargetTable())
                        .addPair("targetColumn", sf.getReferenceTo().getTargetColumn());
            }
        }
        units.add(unit);
        return units;
    }

}
