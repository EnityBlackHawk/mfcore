package org.utfpr.mf.migration;

import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;
import kotlin.Metadata;
import org.utfpr.mf.MockLayer;
import org.utfpr.mf.annotarion.Injected;
import org.utfpr.mf.enums.DefaultInjectParams;
import org.utfpr.mf.llm.ChatAssistant;
import org.utfpr.mf.migration.params.MetadataInfo;
import org.utfpr.mf.migration.params.MigrationSpec;
import org.utfpr.mf.migration.params.Model;
import org.utfpr.mf.prompt.Framework;
import org.utfpr.mf.prompt.MigrationPreferences;
import org.utfpr.mf.prompt.PromptData3;
import org.utfpr.mf.prompt.Query;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

public class GenerateModelStep extends MfMigrationStepEx{

    private final MigrationSpec migrationSpec;

    @Injected(DefaultInjectParams.LLM_KEY)
    private String llm_key;

    public GenerateModelStep(MigrationSpec spec) {
        this(spec, System.out);
    }

    public GenerateModelStep(MigrationSpec spec, PrintStream printStream) {
        super("GenerateModelStep", printStream, MetadataInfo.class, Model.class);
        if(spec == null) {
            throw new IllegalArgumentException("MigrationSpec cannot be null");
        }
        this.migrationSpec = spec;
    }

    @Override
    public Object execute(Object input) {

        MetadataInfo metadataInfo = (MetadataInfo) input;

        assert llm_key != null : "llm_key is not set";

        var gpt = new OpenAiChatModel.OpenAiChatModelBuilder()
                .apiKey(llm_key)
                .modelName(migrationSpec.getLLM())
                .maxRetries(1)
                .temperature(1d)
                .build();
        var gptAssistant = AiServices.builder(ChatAssistant.class).chatLanguageModel(gpt).build();

        var prompt = new PromptData3(
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
        int tokens = 0;
        String resultString;
        ArrayList<String> objs = new ArrayList<>();

        if(MockLayer.isActivated) {
            resultString = MOCK_GENERATE_MODEL;
        }
        else {
            var result = gptAssistant.chat(p);
            tokens = result.tokenUsage().totalTokenCount();
            resultString = result.content().text();
        }

        while (true) {
            int iS = resultString.indexOf("```json");
            if(iS == -1) break;
            iS += 7;
            int iE = resultString.indexOf("```", iS);
            objs.add(resultString.substring(iS, iE));
            resultString = resultString.substring(iE + 3);
        }



//        persistenceService.persist(
//                new TestResultDTO(
//                        prompt.get(),
//                        SpecificationDTO.overrideCardinality(SpecificationDTO.overrideDataSource(gSpecs.getSpecification(), gSpecs.getMetadataInfo().getSql()), gSpecs.getMetadataInfo().getRelations()),
//                        result.content().text(),
//                        result.tokenUsage().totalTokenCount()
//                )
//        );

        var finalResult = objs.stream().reduce("", String::concat);
        return new Model(finalResult, tokens);
    }

    public static final String MOCK_GENERATE_MODEL = """
            ```json
            // Aircraft collection
            {
            	"id": "string",
            	"type": "string",
            	"airline": {
            		"id": "string",
            		"name": "string"
            	},
            	"manufacturer": {
            		"id": "string",
            		"name": "string"
            	},
            	"registration": "string",
            	"max_passengers": "int"
            }
            
            // Airline collection
            {
            	"id": "string",
            	"name": "string"
            }
            
            // Airport collection
            {
            	"id": "string",
            	"name": "string",
            	"city": "string",
            	"country": "string"
            }
            
            // Booking collection
            {
            	"id": "string",
            	"flight": {
            		"number": "string",
            		"airport_from": {
            			"id": "string",
            			"name": "string",
            			"city": "string",
            			"country": "string"
            		},
            		"airport_to": {
            			"id": "string",
            			"name": "string",
            			"city": "string",
            			"country": "string"
            		},
            		"departure_time_scheduled": "timestamp",
            		"departure_time_actual": "timestamp",
            		"arrival_time_scheduled": "timestamp",
            		"arrival_time_actual": "timestamp",
            		"gate": "int",
            		"aircraft": {
            			"id": "string",
            			"type": "string",
            			"registration": "string"
            		},
            		"connects_to": "string"
            	},
            	"passenger": {
            		"id": "string",
            		"first_name": "string",
            		"last_name": "string",
            		"passport_number": "string"
            	},
            	"seat": "string"
            }
            
            // Manufacturer collection
            {
            	"id": "string",
            	"name": "string"
            }
            
            // Passenger collection
            {
            	"id": "string",
            	"first_name": "string",
            	"last_name": "string",
            	"passport_number": "string"
            }
            ```
            """;
}
