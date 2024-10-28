package org.utfpr.mf.migration;

import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;
import kotlin.Metadata;
import org.utfpr.mf.MockLayer;
import org.utfpr.mf.annotarion.Export;
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

public class GenerateModelStep extends MfMigrationStepEx<MetadataInfo, Model>{

    // TODO: Make Injectable
    private final MigrationSpec migrationSpec;

    @Injected(DefaultInjectParams.LLM_KEY)
    private String llm_key;

    @Export(DefaultInjectParams.UNSET)
    private String llm_response;

    @Export(DefaultInjectParams.UNSET)
    private String prompt;

    @Export(DefaultInjectParams.PROMPT_DATA_VERSION)
    private Integer promptDataVersion;

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

    private Model process(MetadataInfo metadataInfo) {

        assert llm_key != null : "llm_key is not set";
        BEGIN("Generating LLM interface");
        var gpt = new OpenAiChatModel.OpenAiChatModelBuilder()
                .apiKey(llm_key)
                .modelName(migrationSpec.getLLM())
                .maxRetries(1)
                .temperature(1d)
                .build();
        var gptAssistant = AiServices.builder(ChatAssistant.class).chatLanguageModel(gpt).build();
        BEGIN("Building prompt");
        promptDataVersion = 3;
        INFO("Using PromptData3");
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
        this.prompt = p;
        int tokens = 0;
        String resultString;
        ArrayList<String> objs = new ArrayList<>();
        BEGIN("Generating model");
        if(MockLayer.isActivated) {
            resultString = MOCK_GENERATE_MODEL;
        }
        else {
            var result = gptAssistant.chat(p);
            tokens = result.tokenUsage().totalTokenCount();
            resultString = result.content().text();
        }
        llm_response = resultString;
        BEGIN("Extracting JSON objects");
        var explanation = extracted(resultString, objs);
        BEGIN("Finalizing model");
        var finalResult = objs.stream().reduce("", String::concat);
        return new Model(finalResult, explanation, tokens);
    }

    @Override
    public Object execute(Object input) {
        return executeHelper(this::process, input);
    }

    public static String extracted(String resultString, ArrayList<String> objs) {
        while (true) {
            int iS = resultString.indexOf("```json");
            if(iS == -1) return resultString.trim();
            iS += 7;
            int iE = resultString.indexOf("```", iS);
            objs.add(resultString.substring(iS, iE));
            resultString = resultString.substring(iE + 3);
        }
    }

    public static final String MOCK_GENERATE_MODEL = """
            ```json
            // Aircraft collection
            {
            	"_id": "string",
            	"type": "string",
            	"registration": "string",
            	"max_passengers": "integer",
            	"airline": {
            		"id": "string",
            		"name": "string"
            	},
            	"manufacturer": {
            		"id": "string",
            		"name": "string"
            	}
            }
            
            // Airline collection
            {
            	"_id": "string",
            	"name": "string"
            }
            
            // Airport collection
            {
            	"_id": "string",
            	"name": "string",
            	"city": "string",
            	"country": "string"
            }
            
            // Flight collection
            {
            	"_id": "string",
            	"number": "string",
            	"departure_time_scheduled": "timestamp",
            	"departure_time_actual": "timestamp",
            	"arrival_time_scheduled": "timestamp",
            	"arrival_time_actual": "timestamp",
            	"gate": "integer",
            	"aircraft": {
            		"id": "string",
            		"type": "string",
            		"registration": "string",
            		"max_passengers": "integer",
            		"airline": {
            			"id": "string",
            			"name": "string"
            		},
            		"manufacturer": {
            			"id": "string",
            			"name": "string"
            		}
            	},
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
            	"connects_to": {
            		"number": "string",
            		"connecting_airport": {
            			"id": "string",
            			"name": "string"
            		}
            	}
            }
            
            // Manufacturer collection
            {
            	"_id": "string",
            	"name": "string"
            }
            
            // Passenger collection
            {
            	"_id": "string",
            	"first_name": "string",
            	"last_name": "string",
            	"passport_number": "string",
            	"bookings": [
            		{
            			"seat": "string",
            			"flight": {
            				"id": "string",
            				"number": "string",
            				"departure_time_scheduled": "timestamp",
            				"airport_from": {
            					"id": "string",
            					"name": "string"
            				},
            				"airport_to": {
            					"id": "string",
            					"name": "string"
            				}
            			}
            		}
            	]
            }
            
            // Booking collection
            {
            	"_id": "string",
            	"flight": {
            		"number": "string"
            	},
            	"passenger": {
            		"id": "string",
            		"first_name": "string",
            		"last_name": "string",
            		"passport_number": "string"
            	},
            	"seat": "string"
            }
            ```
            
            ### Explanation:
            1. **Denormalization**: In MongoDB, it is often efficient to denormalize and embed related data together in a single document when they are frequently accessed together. For example, the `Flight` collection embeds `aircraft`, along with its associated `airline` and `manufacturer` information. This design optimizes for the most frequently used queries which require flight and aircraft information.
            2. **References for Less Frequently Accessed Data**: In our model, the `Passenger` and `Booking` collections reference necessary data rather than embedding all information, as this data is less frequently used and typically required for specific queries (i.e., booking details).\s
            3. **Primary Key to `_id`**: All documents now utilize a string-based `_id` field, in line with MongoDB's document structure, ensuring that every item is uniquely identifiable.
            4. **Maintain Relationships**: The model retains key relationships from the relational structure, like referencing airports and flights, facilitating easy querying while reducing the need for complex joins.
            5. **Performance Optimization**: The design reduces the number of joins by embedding the most frequently queried data together, which improves read performance, especially for the defined usage patterns.
            """;
}
