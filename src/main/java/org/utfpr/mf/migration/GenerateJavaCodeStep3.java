package org.utfpr.mf.migration;

import org.utfpr.mf.migration.params.GeneratedJavaCode;
import org.utfpr.mf.migration.params.Model;
import org.utfpr.mf.orms.ORMTable;
import org.utfpr.mf.prompt.Framework;
import org.utfpr.mf.reflection.ClassMetadataList;
import org.utfpr.mf.reflection.JsonSchemaToClassConverter;
import org.utfpr.mf.reflection.MfClassGenerator;

import java.io.PrintStream;
import java.util.HashMap;

public class GenerateJavaCodeStep3 extends GenerateJavaCodeStep2 {

    public GenerateJavaCodeStep3(PrintStream printStream) {
        super(printStream);
    }

    public GenerateJavaCodeStep3() {
        super();
    }

    @Override
    protected GeneratedJavaCode process(Model model) {

        JsonSchemaToClassConverter converter = new JsonSchemaToClassConverter(ORMTable.getOrmAnnotations(Framework.SPRING_DATA));
        ClassMetadataList metadataList = converter.convertMany(model.getModels());

        MfClassGenerator generator = new MfClassGenerator(metadataList, model.getModels());

        HashMap<String, String> mapResult;

        try {
            mapResult = generator.generate();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        return new GeneratedJavaCode(mapResult, 0);
    }
}
