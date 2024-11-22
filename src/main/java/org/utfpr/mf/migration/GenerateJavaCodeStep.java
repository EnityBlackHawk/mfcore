package org.utfpr.mf.migration;

import org.utfpr.mf.MockLayer;
import org.utfpr.mf.annotation.Injected;
import org.utfpr.mf.enums.DefaultInjectParams;
import org.utfpr.mf.llm.LLMService;
import org.utfpr.mf.migration.params.GeneratedJavaCode;
import org.utfpr.mf.migration.params.Model;
import org.utfpr.mf.prompt.Framework;
import org.utfpr.mf.prompt.PromptData3;
import org.utfpr.mf.tools.ConvertToJavaFile;

import java.io.PrintStream;

public class GenerateJavaCodeStep extends MfMigrationStepEx<Model, GeneratedJavaCode> {

    @Injected(DefaultInjectParams.LLM_KEY)
    private String key;

    @Injected(DefaultInjectParams.LLM_SERVICE)
    private LLMService gptAssistant;

    public GenerateJavaCodeStep() {
        this(System.out);
    }

    public GenerateJavaCodeStep(PrintStream printStream) {
        super("GenerateJavaCodeStep", printStream, Model.class, GeneratedJavaCode.class);
    }

    protected GeneratedJavaCode process(Model model) {
        BEGIN("Building LLM interface");

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

    @Override
    public Object execute(Object input) {
        return executeHelper(this::process, input);
    }

    public static String MOCK_RESPONSE = """
            Here is the Java code representing the MongoDB model you provided, using Lombok annotations and Spring Data MongoDB framework:
            
            ### Aircraft.java
            
            ```java
            import lombok.Data;
            import org.springframework.data.annotation.Id;
            import org.springframework.data.mongodb.core.mapping.Document;
            
            @Document(collection = "aircraft")
            @Data
            public class Aircraft {
             \s
                @Id
                private String id;
                private String type;
                private Airline airline;
                private Manufacturer manufacturer;
                private String registration;
                private Integer maxPassengers;
            
                @Data
                public static class Airline {
                    @Id
                    private String id;
                    private String name;
                }
            
                @Data
                public static class Manufacturer {
                    @Id
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
            
            @Document(collection = "airline")
            @Data
            public class Airline {
             \s
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
            
            @Document(collection = "airport")
            @Data
            public class Airport {
             \s
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
            
            @Document(collection = "booking")
            @Data
            public class Booking {
            
                @Id
                private String id;
                private Flight flight;
                private Passenger passenger;
                private String seat;
            
                @Data
                public static class Flight {
                    @Id
                    private String number;
                }
            }
            ```
            
            ### Flight.java
            
            ```java
            import lombok.Data;
            import org.springframework.data.annotation.Id;
            import org.springframework.data.mongodb.core.mapping.Document;
            
            @Document(collection = "flight")
            @Data
            public class Flight {
             \s
                @Id
                private String id;
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
            
            @Document(collection = "manufacturer")
            @Data
            public class Manufacturer {
             \s
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
            
            @Document(collection = "passenger")
            @Data
            public class Passenger {
             \s
                @Id
                private String id;
                private String firstName;
                private String lastName;
                private String passportNumber;
            }
            ```
            
            ### Notes
            - Each class corresponds to a MongoDB collection as per the provided model.
            - Lombok `@Data` annotation is used to generate getters, setters, equals, hashCode, and toString methods for the entities.
            - The `@Document` annotation specifies the collection name in MongoDB.
            - The inner classes for `Aircraft` now include `Airline` and `Manufacturer`, aligning with the model structure.\s
            
            This code is ready to be used within a Spring Data MongoDB project where these classes would facilitate interactions with the underlying MongoDB documents.
            """;
}
