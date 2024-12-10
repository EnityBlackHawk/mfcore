package org.utfpr.mf.migration;

import com.google.gson.Gson;
import org.utfpr.mf.MockLayer;
import org.utfpr.mf.annotation.Export;
import org.utfpr.mf.enums.DefaultInjectParams;
import org.utfpr.mf.json.JsonSchemaList;
import org.utfpr.mf.llm.LLMResponseJsonSchema;
import org.utfpr.mf.migration.params.MetadataInfo;
import org.utfpr.mf.migration.params.Model;
import org.utfpr.mf.prompt.MigrationPreferences;
import org.utfpr.mf.prompt.PromptData4;
import org.utfpr.mf.prompt.Query;

import java.io.PrintStream;
import java.util.ArrayList;

public class GenerateModelStep2 extends GenerateModelStep{

    @Export(DefaultInjectParams.JSON_SCHEMA_LIST)
    private JsonSchemaList recipes;

    public GenerateModelStep2() {
    }

    public GenerateModelStep2(PrintStream printStream) {
        super(printStream);
    }

    @Override
    protected Model process(MetadataInfo metadataInfo) {

        if(migrationSpec == null) {
            throw new IllegalArgumentException("MigrationSpec not provided");
        }


        BEGIN("Building prompt");
        promptDataVersion = 4;
        INFO("Using PromptData" + promptDataVersion);
        var prompt = new PromptData4(
                metadataInfo.getSql(),
                migrationSpec.getPrioritize_performance() ? MigrationPreferences.PREFER_PERFORMANCE : MigrationPreferences.PREFER_CONSISTENCY,
                migrationSpec.getAllow_ref(),
                migrationSpec.getFramework(),
                metadataInfo.getRelations().toString(),
                true,
                migrationSpec.getWorkload() != null ? migrationSpec.getWorkload().stream().map(Query::new).toList() : new ArrayList<>(),
                migrationSpec.getCustom_prompt() != null ? migrationSpec.getCustom_prompt() : new ArrayList<>()

        );
        var p = prompt.next();
        this.prompt = p;
        int tokens = 0;
        LLMResponseJsonSchema result;
        ArrayList<String> objs = new ArrayList<>();
        BEGIN("Generating model");
        if(MockLayer.isActivated) {
            //resultString = MOCK_GENERATE_MODEL;
            throw new RuntimeException("Not implemented exception");
        }
        else {
            result = gptAssistant.getJsonSchemaList(p);
        }

        Gson gson = new Gson();
        llm_response = gson.toJson(result);
        BEGIN("Finalizing model");
        recipes = result.getSchemas();
        return new Model(gson.toJson(result.getSchemas()), result.getExplanation(), -1, result.getSchemas());
    }
}
