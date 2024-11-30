package org.utfpr.mf.reflection;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.expr.BooleanLiteralExpr;
import com.github.javaparser.ast.expr.ClassExpr;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.Type;
import com.google.gson.Gson;
import kotlin.Pair;
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
    private HashMap<String, CompilationUnit> classes = new HashMap<>();

    public MfClassGenerator(String classMetadataString, @Nullable String model) {
        this.classMetadaString = classMetadataString;
        this.model = model;
        Gson gson = new Gson();
        this.list = gson.fromJson(classMetadaString, ClassMetadataList.class);
        this.schemas = gson.fromJson(model, JsonSchemaList.class);
    }

    public HashMap<String, String> generate() throws ClassNotFoundException {

        for(ClassMetadata cm : list) {
            List<CompilationUnit> units = createClass(cm);
            for(CompilationUnit unit : units) {
                classes.put(unit.getType(0).getNameAsString(), unit);
            }
        }

        annotateClasses(classes);
        HashMap<String, String> finalClasses = new HashMap<>();
        for(Map.Entry<String, CompilationUnit> e : classes.entrySet()) {
            finalClasses.put(e.getKey(), e.getValue().toString());
        }

        return finalClasses;
    }



    private List<CompilationUnit> createClass(ClassMetadata cm) {

        List<CompilationUnit> units = new ArrayList<>();

        CompilationUnit unit = new CompilationUnit();

        var classDec = unit.addClass(cm.getClassName(), Modifier.Keyword.PUBLIC);
        classDec.addAnnotation(Document.class);
        classDec.addAnnotation("lombok.Data");

        for(FieldMetadata fmd : cm.getFields()) {

            String className = fmd.getType();
            if(className.contains("<")) {

                String newClass = className.substring(className.indexOf("<") + 1, className.indexOf(">"));
                className = className.substring(0, className.indexOf("<"));

                var nestedUnit = createClass(list.stream().filter(c -> c.getClassName().equals(newClass)).findFirst().orElseThrow());
                units.addAll(nestedUnit);

            }
            unit.addImport(className);
            var fieldDec = classDec.addPrivateField(fmd.getType(), fmd.getName());

            for(String ann : fmd.getAnnotations()) {
                fieldDec.addAnnotation(ann);
            }

            // TODO Tratar classes duplicadas
            if(!className.contains(".")) {
                String newClass = className;
                var nestedUnit = createClass(list.stream().filter(c -> c.getClassName().equals(newClass)).findFirst().orElseThrow());
                units.addAll(nestedUnit);
            }
        }
        units.add(unit);
        return units;
    }

    private HashMap<String, CompilationUnit> annotateClasses(HashMap<String, CompilationUnit> classes) {

        for(JsonSchema schema : schemas) {

            String className = TemplatedString.capitalize(TemplatedString.camelCaseToSnakeCase(schema.getTitle()));
            CompilationUnit unit = classes.get(className);

            annotateClass(schema, unit, null);

        }


        return classes;
    }

    private void annotateClass(JsonSchema schema, CompilationUnit compilationUnit, @Nullable String name) {

        String className = TemplatedString.capitalize(TemplatedString.camelCaseToSnakeCase(schema.getTitle() != null ? schema.getTitle() : name  ));
        var clazz = compilationUnit.getClassByName(className).orElse(null);

        if(clazz == null) {
            throw new RuntimeException("Class not found: " + className);
        }

        var fields = clazz.getFields();

        for(Map.Entry<String, JsonSchema> eProp : schema.getProperties().entrySet()) {

            String propName = TemplatedString.camelCaseToSnakeCase(eProp.getKey());
            JsonSchema sf = eProp.getValue();

            FieldDeclaration fieldDec = clazz.getFieldByName(TemplatedString.snakeCaseToCamelCase(propName)).orElse(null);

            assert fieldDec != null;

            Type classType = fieldDec.getCommonType();
            ClassExpr classExpr = new ClassExpr(classType);

            // TODO Tratar repeticoes
            // TODO List<Booking> n esta chegando
            if(sf.getType().equals("object") && classes.containsKey(classType.toString())) {
                var cmp = classes.get(classType.toString());
                annotateClass( sf, cmp,  classType.toString());

            }

            Object isReference = sf.getReference();
            String type = sf.getType().toString();
            String column = sf.getColumn();
            String table = sf.getTable();
            var isRef = new BooleanLiteralExpr(isReference != null && Boolean.parseBoolean(isReference.toString()));
            if(column == null || table == null) {
                throw new RuntimeException("Column or table not set for field: " + propName + "on class: " + className + " schema: " + schema.getTitle());
            }

            NormalAnnotationExpr fromRDB = fieldDec.addAndGetAnnotation(FromRDB.class)
                    .addPair("type", new StringLiteralExpr( Objects.equals(type, "object") ? classType.toString() : type ) )
                    .addPair("typeClass", classExpr)
                    .addPair("column", new StringLiteralExpr(sf.getColumn()))
                    .addPair("table", new StringLiteralExpr(sf.getTable()))
                    .addPair("isReference", isRef);
            if(sf.getReferenceTo() != null) {
                fromRDB.addPair("targetTable", new StringLiteralExpr( sf.getReferenceTo().getTargetTable() ))
                        .addPair("targetColumn", new StringLiteralExpr( sf.getReferenceTo().getTargetColumn()));
            }


        }

    }

}
