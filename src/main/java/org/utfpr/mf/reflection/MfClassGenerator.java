package org.utfpr.mf.reflection;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.expr.BooleanLiteralExpr;
import com.github.javaparser.ast.expr.ClassExpr;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.Type;
import com.google.gson.Gson;
import org.jetbrains.annotations.Nullable;
import org.springframework.data.mongodb.core.mapping.Document;
import org.utfpr.mf.annotation.FromRDB;
import org.utfpr.mf.json.JsonSchema;
import org.utfpr.mf.json.JsonSchemaList;
import org.utfpr.mf.tools.QueryResult;
import org.utfpr.mf.tools.TemplatedString;

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
                JsonSchema s = schema.getProperties().get(fmd.getName());
                if(s == null) {
                    s = schema.getProperties().get(TemplatedString.camelCaseToSnakeCase(fmd.getName()));

                    if(s == null) {
                        continue;
                    }
                }
                var nestedUnit = createClass(className, s);
                units.addAll(nestedUnit);
            }

            JsonSchema sf = schema.getProperties().get(fmd.getName());

            if(sf == null) {

                sf = schema.getProperties().get(TemplatedString.camelCaseToSnakeCase(fmd.getName()));
                if (sf == null) {
                    //throw new RuntimeException("Field not found: " + fmd.getName() + "\non class: " + cm.getClassName() + "\nschema: " + schema.getTitle());
                    continue;
                }
            }

            Type classType = new ClassOrInterfaceType(null, className);

            Object isReference = sf.getReference();
            String type = sf.getType().toString();

            ClassExpr classExpr = new ClassExpr(classType);
            String column = sf.getColumn();
            String table = sf.getTable();
            var isRef = new BooleanLiteralExpr(isReference != null && Boolean.parseBoolean(isReference.toString()));

            if(column == null || table == null) {
                throw new RuntimeException("Column or table not set for field: " + fmd.getName() + "on class: " + cm.getClassName() + " schema: " + schema.getTitle());
            }

            NormalAnnotationExpr fromRDB = fieldDec.addAndGetAnnotation(FromRDB.class)
                    .addPair("type", new StringLiteralExpr( Objects.equals(type, "object") ? className : type ) )
                    .addPair("typeClass", classExpr)
                    .addPair("column", new StringLiteralExpr(sf.getColumn()))
                    .addPair("table", new StringLiteralExpr(sf.getTable()))
                    .addPair("isReference", isRef);

            if(sf.getReferenceTo() != null) {
                fromRDB.addPair("targetTable", new StringLiteralExpr( sf.getReferenceTo().getTargetTable() ))
                        .addPair("targetColumn", new StringLiteralExpr( sf.getReferenceTo().getTargetColumn()));
            }
        }
        units.add(unit);
        return units;
    }

}
