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
import org.utfpr.mf.prompt.desc.PromptData4Desc;

import java.io.PrintStream;
import java.util.ArrayList;

public class GenerateModelStep2 extends GenerateModelStep{

    @Export(DefaultInjectParams.JSON_SCHEMA_LIST)
    private JsonSchemaList recipes;

    @Export(DefaultInjectParams.UNSET)
    private String llm_explanation;

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
        promptData4Desc = new PromptData4Desc();
        promptData4Desc.sqlTables = metadataInfo.getSql();
        promptData4Desc.migrationPreference = migrationSpec.getPrioritize_performance() ? MigrationPreferences.PREFER_PERFORMANCE : MigrationPreferences.PREFER_CONSISTENCY;
        promptData4Desc.allowReferences = migrationSpec.getAllow_ref();
        promptData4Desc.framework = migrationSpec.getFramework();
        promptData4Desc.cardinalityTable = metadataInfo.getRelations().toString();
        promptData4Desc.useMarkdown = true;
        promptData4Desc.queryList = migrationSpec.getWorkload() != null ? migrationSpec.getWorkload().stream().map(Query::new).toList() : new ArrayList<>();
        promptData4Desc.customPrompts = migrationSpec.getCustom_prompt() != null ? migrationSpec.getCustom_prompt() : new ArrayList<>();
        promptData4Desc.referenceOnly = migrationSpec.getReference_only();

        var prompt = new PromptData4(promptData4Desc);
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
        llm_explanation = result.getExplanation();
        return new Model(gson.toJson(result.getSchemas()), result.getExplanation(), -1, result.getSchemas());
    }
}
