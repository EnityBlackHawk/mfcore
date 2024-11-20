package org.utfpr.mf.migration;

import org.utfpr.mf.MockLayer;
import org.utfpr.mf.migration.params.GeneratedJavaCode;
import org.utfpr.mf.migration.params.Model;
import org.utfpr.mf.prompt.PromptData4;
import org.utfpr.mf.reflection.MfClassGenerator;
import org.utfpr.mf.tools.ConvertToJavaFile;

import java.io.PrintStream;
import java.util.HashMap;

public class GenerateJavaCodeStep2 extends GenerateJavaCodeStep {

    public GenerateJavaCodeStep2(PrintStream printStream) {
        super(printStream);
    }

    public GenerateJavaCodeStep2() {
        super();
    }

    @Override
    protected GeneratedJavaCode process(Model model) {

        BEGIN("Building LLM interface");

        int token = 0;
        String result;
        HashMap<String, String> mapResult;
        BEGIN("Building prompt");

        var prompt = PromptData4.getSecond(model.getModel(), null);
        var res = gptAssistant.chat(prompt);
        result = res.content().text();
        token = res.tokenUsage().totalTokenCount();

        MfClassGenerator generator = new MfClassGenerator(result);
        
        try {
            mapResult = generator.generate();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        return new GeneratedJavaCode(mapResult, token);
    }

}
