package org.utfpr.mf.reflection;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.google.gson.Gson;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MfClassGenerator {

    private final String classMetadaString;

    public MfClassGenerator(String classMetadataString) {
        this.classMetadaString = classMetadataString;
    }

    public HashMap<String, String> generate() throws ClassNotFoundException {

        HashMap<String, String> classes = new HashMap<>();

        Gson gson = new Gson();
        ClassMetadataList list = gson.fromJson(classMetadaString, ClassMetadataList.class);

        for(ClassMetadata cm : list) {

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
            }

            classes.put( cm.getClassName(), unit.toString());
        }
        return classes;
    }

}
