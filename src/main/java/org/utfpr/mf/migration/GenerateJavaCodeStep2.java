package org.utfpr.mf.migration;

import org.utfpr.mf.annotation.Injected;
import org.utfpr.mf.enums.DefaultInjectParams;
import org.utfpr.mf.llm.LLMService;
import org.utfpr.mf.migration.params.GeneratedJavaCode;
import org.utfpr.mf.migration.params.Model;
import org.utfpr.mf.prompt.PromptData4;
import org.utfpr.mf.reflection.MfClassGenerator;

import java.io.PrintStream;
import java.util.HashMap;

public class GenerateJavaCodeStep2 extends GenerateJavaCodeStep {

    @Injected(DefaultInjectParams.LLM_KEY)
    private String key;

    @Injected(DefaultInjectParams.LLM_SERVICE)
    private LLMService gptAssistant;

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
        var res = gptAssistant.getJson(prompt);
        result = res;
        token = -1;

        MfClassGenerator generator = new MfClassGenerator(result, model.getModels());

        try {
            mapResult = generator.generate();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        return new GeneratedJavaCode(mapResult, token);
    }

}
