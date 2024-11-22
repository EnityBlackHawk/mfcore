package org.utfpr.mf.prompt;

import org.jetbrains.annotations.Nullable;
import org.utfpr.mf.markdown.MarkdownContent;
import org.utfpr.mf.metadata.DbMetadata;

import javax.naming.OperationNotSupportedException;
import java.util.ArrayList;
import java.util.List;

public class PromptData4 extends PromptData3 {

    public PromptData4(DbMetadata dbMetadata, MigrationPreferences migrationPreference, Boolean allowReferences, Framework framework, @Nullable String cardinalityTable, Boolean useMarkdown, List<Query> queryList, List<String> remarks) {
        super(dbMetadata, migrationPreference, allowReferences, framework, cardinalityTable, useMarkdown, queryList, remarks);
    }

    public PromptData4(String sqlTables, MigrationPreferences migrationPreference, Boolean allowReferences, Framework framework, @Nullable String cardinalityTable, Boolean useMarkdown, List<Query> queryList, List<String> customPrompts) {
        super(sqlTables, migrationPreference, allowReferences, framework, cardinalityTable, useMarkdown, queryList, customPrompts);
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
        sb.append("""
                // Aircraft collection
                {
                	"id" : {"type" :  "string", "table" : "aircraft", "column" : "id" },
                	"model" : {"type" :  "string", "table" : "aircraft", "column" : "model" },
                    "airline": {"type" : "DBRef", "table" : "airline", "column" : "id"},
                	"manufacturer" : {
                		"id" : {"type" :  "string", "table" : "manufacturer", "column" : "id" },
                		"name" : {"type" :  "string", "table" : "manufacturer", "column" : "name" },
                	}
                }
                """).append("\n");
        sb.append("```").append("\n");

        sb.append("Please generate only the MongoDB model in JSON format based on the provided details. And a little explanation of why you choose this model.").append("\n");

        return sb.toString();
    }

    public static String getSecond(String jsonDocuments, @Nullable Framework framework) {

        MarkdownContent mc = new MarkdownContent();
        mc.addCodeBlock(jsonDocuments, "json");
        mc.addPlainText("Analyze the provided JSON schema and generate a mapping in the following format:", '\n');
        mc.addPlainText("""
                [
                    {
                      "className": "User",
                      "fields": [
                        {
                          "name": "id",
                          "type": "java.lang.String",
                          "annotations": ["org.springframework.data.annotation.Id"]
                        }
                      ]
                    },
                    ...
                ]
                """, '\n');
        mc.addPlainText("Ensure all fields in the JSON schema are accurately mapped to their corresponding Java data types. " +
                "Additionally, include relevant annotations (e.g., @Id, @NotNull, etc.) for the fields where applicable, based on the context provided in the schema. " +
                "The output should strictly follow the given structure.", '\0');


        return mc.toString();
    }

}
