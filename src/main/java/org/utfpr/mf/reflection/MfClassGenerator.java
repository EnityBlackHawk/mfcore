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
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.utfpr.mf.annotation.FromRDB;
import org.utfpr.mf.json.JsonSchema;
import org.utfpr.mf.json.JsonSchemaList;
import org.utfpr.mf.tools.CodeSession;
import org.utfpr.mf.tools.QueryResult;
import org.utfpr.mf.tools.TemplatedString;

import java.io.PrintStream;
import java.util.*;

public class MfClassGenerator extends CodeSession {

    private final String classMetadaString;
    private final ClassMetadataList list;
    private final JsonSchemaList schemas;
    private HashMap<String, CompilationUnit> classes = new HashMap<>();

    public MfClassGenerator(String classMetadataString, JsonSchemaList model) {
        this(classMetadataString, model, System.out);
    }

    public MfClassGenerator(String classMetadataString, JsonSchemaList model, PrintStream printStream) {
        super("MfClassGenerator", printStream);

        this.classMetadaString = classMetadataString;
        Gson gson = new Gson();
        this.list = gson.fromJson(classMetadaString, ClassMetadataList.class);
        this.schemas = model;
    }

    public HashMap<String, String> generate() throws ClassNotFoundException {

        for(ClassMetadata cm : list) {
            List<CompilationUnit> units = createClass(cm);
            for(CompilationUnit unit : units) {
                classes.put(unit.getType(0).getNameAsString(), unit);
            }
        }

        annotateClasses(classes);
        verifyAnnotations();
        HashMap<String, String> finalClasses = new HashMap<>();
        for(Map.Entry<String, CompilationUnit> e : classes.entrySet()) {
            finalClasses.put(e.getKey(), e.getValue().toString());
        }

        return finalClasses;
    }

    private void verifyAnnotations() {
        BEGIN("Verifying classes");
        for(var clazz : classes.values()) {
            BEGIN_SUB(clazz.getType(0).getNameAsString());
            var fields = clazz.getType(0).getFields();
            for(var field : fields) {
                if(field.getAnnotationByClass(FromRDB.class).isEmpty()) {
                    INFO("Field not annotated: " + field.getVariable(0).getNameAsString() + ". This field will be ignored on migration.");
                }
            }
        }
    }

    private List<CompilationUnit> createClass(ClassMetadata cm) {

        List<CompilationUnit> units = new ArrayList<>();

        CompilationUnit unit = new CompilationUnit();

        var classDec = unit.addClass(cm.getClassName(), Modifier.Keyword.PUBLIC);
        classDec.addAnnotation(Document.class);
        classDec.addAnnotation("lombok.Data");

        for(FieldMetadata fmd : cm.getFields()) {

            String className = fmd.getType();
            String pureClassName = className;
            if(className.contains("<")) {
                pureClassName = className.substring(0, className.indexOf("<"));
            }
            unit.addImport(pureClassName);
            var fieldDec = classDec.addPrivateField(fmd.getType(), fmd.getName());

            for(String ann : fmd.getAnnotations()) {
                fieldDec.addAnnotation(ann);
            }

        }
        units.add(unit);
        return units;
    }

    private HashMap<String, CompilationUnit> annotateClasses(HashMap<String, CompilationUnit> classes) {

        for(JsonSchema schema : schemas) {

            String className = TemplatedString.capitalize(TemplatedString.camelCaseToSnakeCase(schema.getTitle()));
            CompilationUnit unit = classes.get(className);

            annotateClass(schema, unit, null, schema.getTitle());

        }


        return classes;
    }

    private void annotateClass(JsonSchema schema, CompilationUnit compilationUnit, @Nullable String name, String docName) {

        String className = TemplatedString.capitalize(TemplatedString.snakeCaseToCamelCase(schema.getTitle() != null ? schema.getTitle() : name  ));
        var clazz = compilationUnit.getClassByName(className).orElse(null);
        BEGIN("Annotating class: " + className);

        if(clazz == null) {
            throw new RuntimeException("Class not found: " + className);
        }

        var props = schema.getProperties();

        if(props == null) {
            ERROR("No properties found for class: " + className);
            return;
        }

        for(Map.Entry<String, JsonSchema> eProp : schema.getProperties().entrySet()) {

            String propName = TemplatedString.camelCaseToSnakeCase(eProp.getKey());
            JsonSchema sf = eProp.getValue();

            FieldDeclaration fieldDec = clazz.getFieldByName(TemplatedString.snakeCaseToCamelCase(propName)).orElse(null);

            if(fieldDec == null) {
                // TODO Notify (Do Log)
                continue;
            }

            if(fieldDec.getAnnotationByClass(FromRDB.class).isPresent()) {
                INFO("Field already annotated: " + propName);
                continue;
            }


            Type classType = fieldDec.getCommonType();
            ClassExpr classExpr = new ClassExpr(classType);

            // TODO Tratar repeticoes
            // TODO List<Booking> n esta chegando
            if(sf.getType().equals("object") && classes.containsKey(classType.toString())) {
                var cmp = classes.get(classType.toString());
                annotateClass( sf, cmp,  classType.toString(), docName);

            }

            Boolean isReference = Objects.requireNonNullElse(sf.getReference(), false);
            String type = sf.getType().toString();
            String column = sf.getColumn();
            String table = sf.getTable();
            Boolean isAbstract = sf.getIsAbstract();

            var isRef = new BooleanLiteralExpr(Boolean.parseBoolean(isReference.toString()));
            if(column == null || table == null) {
                //throw new RuntimeException("Column or table not set for field: " + propName + "on class: " + className + " schema: " + schema.getTitle());
                INFO("Column or table not set for field: " + propName + " on class: " + className + " schema: " + schema.getTitle());
                column = "";
                table = "";
                isAbstract = true;
            }

            if(isReference) {
                fieldDec.addAnnotation(DBRef.class);
            }

            String classStringName = classExpr.toString();

            if(classStringName.contains("<")) {
                String tmp = classStringName.substring(classStringName.indexOf(">") + 1);
                classStringName = classStringName.substring(0, classStringName.indexOf("<")).concat(tmp);

            }

            NormalAnnotationExpr fromRDB = fieldDec.addAndGetAnnotation(FromRDB.class)
                    .addPair("type", new StringLiteralExpr( Objects.equals(type, "object") ? classType.toString() : type ) )
                    .addPair("typeClass", classStringName)
                    .addPair("column", new StringLiteralExpr(column))
                    .addPair("table", new StringLiteralExpr(table))
                    .addPair("isReference", isRef)
                    .addPair("isAbstract", new BooleanLiteralExpr(Objects.requireNonNullElse(isAbstract, false)))
                    .addPair("projection", new StringLiteralExpr(Objects.requireNonNullElse(sf.getProjection(), "*")));
            if(sf.getReferenceTo() != null) {
                fromRDB.addPair("targetTable", new StringLiteralExpr( sf.getReferenceTo().getTargetTable() ))
                        .addPair("targetColumn", new StringLiteralExpr( sf.getReferenceTo().getTargetColumn()));
            }


        }

    }

}
