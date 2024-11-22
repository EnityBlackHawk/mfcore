package org.utfpr.mf.reflection;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import javassist.util.proxy.RuntimeSupport;
import lombok.Data;
import org.jetbrains.annotations.Nullable;
import org.springframework.data.mongodb.core.mapping.Document;
import org.utfpr.mf.annotation.FromRDB;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MfClassGenerator {

    private final String classMetadaString;
    private final String model;
    private final ClassMetadataList list;
    private final DocumentRecipeList recipes;

    public MfClassGenerator(String classMetadataString, @Nullable String model) {
        this.classMetadaString = classMetadataString;
        this.model = model;
        Gson gson = new Gson();
        this.list = gson.fromJson(classMetadaString, ClassMetadataList.class);
        this.recipes = gson.fromJson(model, DocumentRecipeList.class);
    }

    public HashMap<String, String> generate() throws ClassNotFoundException {

        HashMap<String, String> classes = new HashMap<>();

        for(DocumentRecipe recipe : recipes) {

            ClassMetadata cm = list.stream().filter(c -> c.getClassName().equals(recipe.get__collection__())).findFirst().orElse(null);
            if(cm == null) {
                throw new ClassNotFoundException("Class not found: " + recipe.get__collection__());
            }


            List<CompilationUnit> units = createClass(cm,  recipe.getFields());
            for(CompilationUnit unit : units) {
                classes.put(unit.getType(0).getNameAsString(), unit.toString());
            }
        }
        return classes;
    }

    private List<CompilationUnit> createClass(String name, Map<String, Map<String, ?>> fields) {

        var cm = list.stream().filter(c -> c.getClassName().equals(name)).findFirst().orElse(null);
        if(cm == null) {
            throw new RuntimeException("Class not found: " + name);
        }
        return createClass(cm, fields);

    }

    private List<CompilationUnit> createClass(ClassMetadata cm, Map<String, Map<String, ?>> fields) {

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

            if(!className.contains(".")) {
                var nestedUnit = createClass(className, (Map<String, Map<String, ?>>) fields.get(fmd.getName()));
                units.addAll(nestedUnit);
                continue;
            }

            Map<String, ?> sf = fields.get(fmd.getName());

            fieldDec.addAndGetAnnotation(FromRDB.class)
                    .addPair("type", sf.get("type").toString())
                    .addPair("column", sf.get("column").toString())
                    .addPair("table", sf.get("table").toString());

        }
        units.add(unit);
        return units;
    }

}
