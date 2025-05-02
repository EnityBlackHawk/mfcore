package org.utfpr.mf.prompt;

import org.jetbrains.annotations.Nullable;
import org.utfpr.mf.annotation.Export;
import org.utfpr.mf.enums.DefaultInjectParams;
import org.utfpr.mf.markdown.MarkdownContent;
import org.utfpr.mf.metadata.DbMetadata;
import org.utfpr.mf.prompt.desc.PromptData4Desc;

import javax.naming.OperationNotSupportedException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PromptData4 extends PromptData3 {

    private Boolean referenceOnly = false;

    @Export(DefaultInjectParams.UNSET)
    private PromptData4Desc promptData4Desc;

    public PromptData4(DbMetadata dbMetadata, MigrationPreferences migrationPreference, Boolean allowReferences, Framework framework, @Nullable String cardinalityTable, Boolean useMarkdown, List<Query> queryList, List<String> remarks) {
        super(dbMetadata, migrationPreference, allowReferences, framework, cardinalityTable, useMarkdown, queryList, remarks);
        referenceOnly = promptData4Desc.referenceOnly;
    }

    public PromptData4(PromptData4Desc desc) {
        super(desc.sqlTables, desc.migrationPreference, desc.allowReferences, desc.framework, desc.cardinalityTable, desc.useMarkdown, desc.queryList, desc.customPrompts);
        this.referenceOnly = desc.referenceOnly;
        this.promptData4Desc = desc;
    }

    public String getFirst() {
        MarkdownContent c = new MarkdownContent();

        c.addPlainText("You are an expert in database modeling. Your task is to help migrate a relational database to MongoDB by generating a custom JSON Schema for each collection, optimized for access patterns and document modeling best practices.", '\n');

        c.addTitle3("Goal");
        c.addPlainText("Generate an optimized MongoDB schema based on:", '\n');
        c.addListItem("The relational schema and its relationships");
        c.addListItem("Cardinality information");
        c.addListItem("Frequently used queries");
        c.addListItem("Data access frequency" + (allowReferences ? "(embed vs reference)" : ""));
        c.addListItem("A custom JSON Schema output format");

        c.addTitle3("Input: Relational Schema");
        c.addCodeBlock(sqlTables, "sql");

        c.addTitle3("Input: Cardinality Information");
        c.addCodeBlock(Objects.requireNonNullElse(cardinalityTable, ""), "json");

        if(queryList != null && !queryList.isEmpty()) {
            c.addTitle3("Input: Workload");
            for (Query query : queryList) {
                c.addListItem(String.format("**Used %s%% of the time:**", query.regularity()));
                c.addCodeBlock(query.query(), "sql");
                c.addPlainText("", '\n');
            }
        }

        c.addTitle3("Embedding vs Referencing Rules:");
        c.addListItem("If two entities are frequently accessed together and the embedded data is small and stable, embed the related data inside the main document.");
        c.addListItem("If the related data is large, frequently updated, or shared across many documents, reference it instead.");

        c.addTitle3("Modeling Guidelines");
        if(allowReferences && migrationPreference == MigrationPreferences.PREFER_PERFORMANCE) {
            c.addListItem("Use embedded documents for data that is frequently accessed together.");
            c.addListItem("Use references (DBRef style) for data that is less frequently accessed.");
            c.addListItem("Avoid references to maintain consistency and reduce complexity.");
            c.addListItem("Use cardinality to guide decisions (e.g. high cardinality suggests using references).");
        } else if (allowReferences && referenceOnly) {
            c.addListItem("**Use references only**. Do not embed any data.");
        } else if (allowReferences && migrationPreference == MigrationPreferences.PREFER_CONSISTENCY) {
            c.addListItem("Use references for data that is frequently updated. Regardless the access pattern.");
            c.addListItem("Use cardinality to guide decisions.");
        }
        else if(!allowReferences) {
            c.addListItem("**Embed all data** to avoid references.");
        }

        c.addListItem("Model to optimize the frequently used queries above.");

        c.addTitle3("Output Format");
        c.addPlainText("Generate a list of JSON Schemas, one for each MongoDB collection. Use the format below (no comments in output!)", '\n');
        c.addCodeBlock("""
                [
                  {
                    "$schema": "http://json-schema.org/draft-07/schema#",
                    "type": "object",
                    "title": "TableName",
                    "description": "Description of the collection",
                    "properties": {
                      "id": {
                        "type": "string",
                        "column": "id",
                        "isId": true,
                        "table": "TableName",
                        "description": "Primary key"
                      },
                      "property_name": {
                        "type": "object | string | number | boolean",
                        "relationshipType": "embedded | reference | none",
                        "column": "column_name",
                        "table": "TableName",
                        "description": "Describe the field",
                        "docReferenceTo": "TargetCollectionName",
                        "referenceTo": {
                          "targetTable": "ForeignTableName",
                          "targetColumn": "PrimaryKey"
                        },
                        "properties": { ... },   // required if embedded
                        "items": { ... }        // required if array
                      },
                      "list": {
                        "type": "array",
                        "column": "column_name",
                        "table": "TableName",
                        "relationshipType": "embedded",
                        "referenceTo": {
                                "targetTable": "ForeignTableName",
                                "targetColumn": "PrimaryKey"
                        }
                        "items": {
                            "type": "object",
                            "relationshipType": "embedded",
                            "column": "column_name",
                            "table": "TableName",
                            "properties": { ... },
                        }
                      }
                    }
                  }
                ]
                """, "json");

        c.addTitle3("Important Constraints");
        c.addListItem("All fields must have: type, column, table.");
        c.addListItem("All objects **must use referenceTo**.");
        c.addListItem("All objects must have **only one** property with isId: true.");
        c.addListItem("All properties with isId: true must be `type: string`.");
        c.addListItem("Embedded documents: relationshipType must be set to `embedded` and must include properties.");

        if(allowReferences) {
            c.addListItem("References: `\"relationshipType\": \"reference\"` with docReferenceTo");
        }

        c.addListItem("If a structure does not exist in the relational schema, add `\"isAbstract\": true.`");

        c.addListItem("For arrays:");
        c.addSubListItem("use `\"type\": \"array\"`");
        c.addSubListItem("Use `\"items\": { ... }` to describe the array items.");
        c.addSubListItem("Use `\"relationshipType\": \"embedded\"`.");
        c.addSubListItem("Add the `referenceTo` property with `targetTable` and `targetColumn`");

        c.addTitle3("Instructions");
        c.addListItem("Analyze the access patterns and cardinality to decide what to embed and what to reference.");
        c.addListItem("Match field and table names exactly as they appear in the schema.");
        c.addListItem("Start with a short explanation (2–3 sentences) about why the schema was modeled that way.");
        c.addListItem("After the explanation, generate the MongoDB model in JSON Schema format as shown above following your explanation.");
        c.addListItem("Output only the valid JSON list of schemas (no Markdown, no explanations, no comments).");


        return c.get();
    }

    public static String getSecond(String jsonDocuments, @Nullable Framework framework) {

        MarkdownContent mc = new MarkdownContent();
        mc.addCodeBlock(jsonDocuments, "json");
        mc.addPlainText("Analyze the provided JSON schema and generate a mapping in the following format:", '\n');
        mc.addCodeBlock("""
            [
              {
                "className": "Student",
                "annotations": ["org.springframework.data.mongodb.core.mapping.Document"],
                "fields": [
                  {
                    "name": "id",
                    "type": "java.lang.String",
                    "annotations": ["org.springframework.data.annotation.Id"]
                  },
                  {
                    "name": "name",
                    "type": "java.lang.String",
                    "annotations": []
                  },
                  {
                    "name": "address",
                    "type": "Address", // Is not a primitive type or a default Java type, so its needs to be declared too
                    "annotations": []
                  }
                ]
              },
              {
                "className": "Address",
                "annotations": [],
                "fields": [
                  {
                    "name": "street",
                    "type": "java.lang.String",
                    "annotations": []
                  },
                  {
                    "name": "city",
                    "type": "java.lang.String",
                    "annotations": []
                  },
                  {
                    "name" : "number",
                    "type": "java.lang.String",
                    "annotations": []
                  }
                ]
              }
            ]
            """, "json");
        mc.addTitle3("Instructions");
        mc.addListItem("Ensure all fields in the JSON schema are accurately mapped to their corresponding Java data types.");
        mc.addListItem("Include relevant annotations (e.g., @Id, @NotNull, etc.) for the fields where applicable, based on the context provided in the schema.");
        mc.addListItem("The output should strictly follow the given structure.");
        mc.addListItem("If the field type is a custom class (e.g., Address), ensure that the class is also included in the mapping.");

        return mc.toString();
    }

}
