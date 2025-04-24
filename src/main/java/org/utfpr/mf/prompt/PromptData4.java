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
        c.addListItem("Data access frequency (embed vs reference)");
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

        c.addTitle3("Modeling Guidelines");
        c.addListItem("Modeling Guidelines");
        c.addListItem("Use embedded documents for data that is frequently accessed together.");
        c.addListItem("Use references (DBRef style) for data that is less frequently accessed.");
        c.addListItem("Use cardinality to guide decisions (e.g. high cardinality suggests using references).");
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
                        "type": "object | string | number | boolean | array",
                        "column": "column_name",
                        "table": "TableName",
                        "description": "Describe the field",
                        "reference": true | false,
                        "docReferenceTo": "TargetCollectionName",
                        "referenceTo": {
                          "targetTable": "ForeignTableName",
                          "targetColumn": "PrimaryKey"
                        },
                        "properties": { ... },   // required if embedded
                        "items": { ... },        // required if array
                        "projection": "column"   // required if simple reference
                      }
                    }
                  }
                ]
                """, "json");

        c.addTitle3("Important Constraints");
        c.addListItem("All fields must have: type, column, table.");
        c.addListItem("All foreign keys must use referenceTo.");
        c.addListItem("All objects must have one isId: true field.");
        c.addListItem("Embedded documents: reference: false and must include properties.");
        c.addListItem("References: reference: true with docReferenceTo.");
        c.addListItem("If a structure does not exist in the relational schema, add `\"isAbstract\": true.`");
        c.addListItem("For arrays, use `\"type\": \"array\"` and include items.");

        c.addTitle3("Instructions");
        c.addListItem("Analyze the access patterns and cardinality to decide what to embed and what to reference.");
        c.addListItem("Match field and table names exactly as they appear in the schema.");
        c.addListItem("Output only the valid JSON list of schemas (no Markdown, no explanations, no comments).");
        c.addListItem("After the JSON, include a short explanation (2–3 sentences) about why the schema was modeled that way.");


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
