package org.utfpr.mf.migration;

import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiChatModelName;
import dev.langchain4j.service.AiServices;
import org.utfpr.mf.MockLayer;
import org.utfpr.mf.annotarion.Injected;
import org.utfpr.mf.enums.DefaultInjectParams;
import org.utfpr.mf.llm.ChatAssistant;
import org.utfpr.mf.migration.params.GeneratedJavaCode;
import org.utfpr.mf.migration.params.Model;
import org.utfpr.mf.prompt.Framework;
import org.utfpr.mf.prompt.PromptData3;
import org.utfpr.mf.tools.ConvertToJavaFile;

import java.io.PrintStream;

public class GenerateJavaCodeStep extends MfMigrationStepEx{

    @Injected(DefaultInjectParams.LLM_KEY)
    private String key;


    public GenerateJavaCodeStep() {
        this(System.out);
    }

    public GenerateJavaCodeStep(PrintStream printStream) {
        super("GenerateJavaCodeStep", printStream, Model.class, GeneratedJavaCode.class);
    }

    @Override
    public Object execute(Object input) {

        Model model = (Model) input;
        BEGIN("Building LLM interface");
        var gpt = new OpenAiChatModel.OpenAiChatModelBuilder()
                .apiKey(key)
                .modelName(OpenAiChatModelName.GPT_4_O_MINI)
                .maxRetries(1)
                .temperature(1d)
                .build();
        var gptAssistant = AiServices.builder(ChatAssistant.class).chatLanguageModel(gpt).build();
        int token = 0;
        String result;
        BEGIN("Building prompt");
        if(MockLayer.isActivated) {
            result = MOCK_RESPONSE;
        }
        else {
            var prompt = PromptData3.getSecond(model.getModel(), Framework.SPRING_DATA);
            var res = gptAssistant.chat(prompt);
            result = res.content().text();
            token = res.tokenUsage().totalTokenCount();
        }
        BEGIN("Parsing response");
        var mapResult = ConvertToJavaFile.toMap(result);
        return new GeneratedJavaCode(mapResult, token);
    }

    public static String MOCK_RESPONSE = """
            Sure! Below are the Java classes representing the MongoDB model you provided, using Lombok annotations for brevity and together with Spring Data MongoDB annotations.
            
            ### Aircraft.java
            ```java
            import lombok.Data;
            import org.springframework.data.annotation.Id;
            import org.springframework.data.mongodb.core.mapping.Document;
            
            @Data
            @Document(collection = "aircraft")
            public class Aircraft {
            
                @Id
                private String id;
                private String type;
                private Airline airline;
                private Manufacturer manufacturer;
                private String registration;
                private Integer maxPassengers;
            
                @Data
                public static class Airline {
                    private String id;
                    private String name;
                }
            
                @Data
                public static class Manufacturer {
                    private String id;
                    private String name;
                }
            }
            ```
            
            ### Airline.java
            ```java
            import lombok.Data;
            import org.springframework.data.annotation.Id;
            import org.springframework.data.mongodb.core.mapping.Document;
            
            @Data
            @Document(collection = "airline")
            public class Airline {
            
                @Id
                private String id;
                private String name;
            }
            ```
            
            ### Airport.java
            ```java
            import lombok.Data;
            import org.springframework.data.annotation.Id;
            import org.springframework.data.mongodb.core.mapping.Document;
            
            @Data
            @Document(collection = "airport")
            public class Airport {
            
                @Id
                private String id;
                private String name;
                private String city;
                private String country;
            }
            ```
            
            ### Booking.java
            ```java
            import lombok.Data;
            import org.springframework.data.annotation.Id;
            import org.springframework.data.mongodb.core.mapping.Document;
            
            @Data
            @Document(collection = "booking")
            public class Booking {
            
                @Id
                private String id;
                private Flight flight;
                private Passenger passenger;
                private String seat;
            
                @Data
                public static class Flight {
                    private String number;
                    private String departureTimeScheduled;
                    private String departureTimeActual;
                    private String arrivalTimeScheduled;
                    private String arrivalTimeActual;
                    private Integer gate;
                    private Airport airportFrom;
                    private Airport airportTo;
                    private Aircraft aircraft;
                    private ConnectsTo connectsTo;
            
                    @Data
                    public static class ConnectsTo {
                        private String number;
                    }
                }
            
                @Data
                public static class Passenger {
                    private String id;
                    private String firstName;
                    private String lastName;
                    private String passportNumber;
                }
            }
            ```
            
            ### Flight.java
            ```java
            import lombok.Data;
            import org.springframework.data.annotation.Id;
            import org.springframework.data.mongodb.core.mapping.Document;
            
            @Data
            @Document(collection = "flight")
            public class Flight {
            
                @Id
                private String number;
                private Airport airportFrom;
                private Airport airportTo;
                private String departureTimeScheduled;
                private String departureTimeActual;
                private String arrivalTimeScheduled;
                private String arrivalTimeActual;
                private Integer gate;
                private Aircraft aircraft;
                private ConnectsTo connectsTo;
            
                @Data
                public static class ConnectsTo {
                    private String number;
                }
            }
            ```
            
            ### Manufacturer.java
            ```java
            import lombok.Data;
            import org.springframework.data.annotation.Id;
            import org.springframework.data.mongodb.core.mapping.Document;
            
            @Data
            @Document(collection = "manufacturer")
            public class Manufacturer {
            
                @Id
                private String id;
                private String name;
            }
            ```
            
            ### Passenger.java
            ```java
            import lombok.Data;
            import org.springframework.data.annotation.Id;
            import org.springframework.data.mongodb.core.mapping.Document;
            
            @Data
            @Document(collection = "passenger")
            public class Passenger {
            
                @Id
                private String id;
                private String firstName;
                private String lastName;
                private String passportNumber;
            }
            ```
            
            ### Summary
            - The above classes represent different collections in your MongoDB model, using Lombok’s `@Data` annotation to generate getters and setters automatically.
            - Each class is annotated with `@Document` to specify the corresponding MongoDB collection.
            - Nested classes are defined where appropriate (e.g., `Airline` in `Aircraft`, `Flight` and `Passenger` in `Booking`) to organize related entities.\s
            
            Make sure you have Lombok and Spring Data MongoDB dependencies added to your project to use this code.
            """;
}
