package org.utfpr.mf.prompt;

import org.jetbrains.annotations.Nullable;
import org.utfpr.mf.metadata.DbMetadata;

import java.util.List;

public class PromptData3 extends PromptData2{


    public PromptData3(DbMetadata dbMetadata, MigrationPreferences migrationPreference, Boolean allowReferences, Framework framework, @Nullable String cardinalityTable, Boolean useMarkdown, List<Query> queryList, List<String> remarks) {
        super(dbMetadata, migrationPreference, allowReferences, framework, cardinalityTable, useMarkdown, queryList, remarks);
        maxCalls = 1;
    }

    public PromptData3(String sqlTables, MigrationPreferences migrationPreference, Boolean allowReferences, Framework framework, @Nullable String cardinalityTable, Boolean useMarkdown, List<Query> queryList, List<String> customPrompts) {
        super(sqlTables, migrationPreference, allowReferences, framework, cardinalityTable, useMarkdown, queryList, customPrompts);
        maxCalls = 1;
    }

    @Override
    public String get() {
        return getFirst();
    }

    public String getFirst() {
        StringBuilder sb = new StringBuilder();
        var infos = getSqlTablesAndQueries();
        sb.append("You are an expert in database modeling. Your task is to help migrate a relational database to a MongoDB database. " +
                        "Follow the instructions and details provided below to generate the MongoDB model. " +
                        "making sure to" +
                        (migrationPreference == MigrationPreferences.PREFER_CONSISTENCY ? " **use references for less frequently accessed data** as instructed. " : " **embed frequently accessed data** into the main documents for efficiency.") +
                        "\n")
                .append("### Task Overview\n")
                .append("We have a relational database that needs to be migrated to MongoDB. The goal is to create an optimized MongoDB schema based on the usage patterns of the data. \n");

        sb.append("### Relational Database Schema").append("\n");
        sb.append("Here is the schema of our current relational database:").append("\n");
        sb.append("```sql").append("\n");
        sb.append(infos.getFirst()).append("\n");
        sb.append("```").append("\n");

        sb.append("### MongoDB Model Considerations").append("\n");

        sb.append("-    **Critically ensure** the use of " +
                (migrationPreference == MigrationPreferences.PREFER_CONSISTENCY
                        ? "**references (DBRef)** to reduce redundancy."
                        : "**embedded documents** to optimize read performance.")).append("\n");

        if(queryList != null) {
            sb.append("-    Optimize for the following frequently used queries:").append("\n");
            for(var query : queryList) {
                sb.append("\t- ").append("**Used ").append(query.regularity()).append("% of the time:** ").append(query.query()).append("\n");
            }
        }
        sb.append( allowReferences ? "- Use references for less frequently accessed data \n" : "");
        sb.append("- ").append( migrationPreference.getDescription()).append("\n");
        sb.append("- **AWAYS** convert the primary key to the be a string \n");

        if(userDefinedPrompts != null) {
            for(var x : userDefinedPrompts)
                sb.append("- ").append(x).append("\n");
        }

        sb.append("### Output format").append("\n");
        sb.append("MongoDB models in JSON format as the example:").append("\n");
        sb.append("```json").append("\n");
        sb.append("// Aircraft collection");
        sb.append("{").append("\n");
        sb.append("\tid : string").append("\n");
        sb.append("\tmodel : string").append("\n");
        sb.append("\tmanufacturer : {").append("\n");
        sb.append("\t\tid : string").append("\n");
        sb.append("\t\tname : string").append("\n");
        sb.append("\t}").append("\n");
        sb.append("}").append("\n");
        sb.append("```").append("\n");

        sb.append("Please generate only the MongoDB model in JSON format based on the provided details. And a little explanation of why you choose this model.").append("\n");

        return sb.toString();
    }

    @Deprecated
    public String getSecond() {
        return getSecond(sqlTables, framework);
    }


    public static String getSecond(String model, Framework framework) {
        String sb = "Generate Java classes this model of MongoDB database: \n" + model + "\n" +
                "### Java Code Requirements" + "\n" +
                "- Generate Java classes for the MongoDB model" + "\n" +
                "- Use Lombok annotations for data classes" + "\n" +
                "- Use " + framework.getFramework() + " framework for the Java code" + "\n" +
                "- All classes must their id property of type String annotated with @Id annotation, even in the inner classes" + "\n" +
                "- Timestamp and dates types must be converted to LocalDataTime" + "\n" +
                "- Add an import statement for all classes and annotations used" + "\n" +
                // "- Use @DBRef annotation for **referencing documents only**" + "\n" +
                "### Output format" + "\n" +
                "- Java code in separate classes for each entity" + "\n" +
                "Please generate the Java code for the MongoDB model based on provided details following the example:" + "\n" +
                """
                ```java
                public class Booking {
            
                    @Id
                    private String id;
                    private Flight flight;
                    @DBRef
                    private Passenger passenger;
                    private String seat;
                
                    @Data
                    public static class Flight {
                        @Id
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
                    }
                }
                 ```""";
        return sb;
    }


    @Override
    public String next() {
        return getFirst();
    }

}
