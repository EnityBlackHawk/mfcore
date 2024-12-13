package org.utfpr.mf.prompt;

import org.jetbrains.annotations.Nullable;
import org.utfpr.mf.markdown.MarkdownContent;
import org.utfpr.mf.metadata.DbMetadata;

import javax.naming.OperationNotSupportedException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
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
        sb.append("MongoDB models in JSON Schema **MUST FOLLOW** the format:").append("\n");
        sb.append("```json").append("\n");
        sb.append("""
                {
                  "explanation": "The MongoDB model embeds frequently accessed data such as Albums and Customers for performance optimization. Less accessed data like Tracks, MediaType, and Genre are referenced to avoid data duplication and maintain scalability.",
                  "schemas": [
                    {
                      "$schema": "http://json-schema.org/draft-07/schema#",
                      "type": "object",
                      "title": "Album",
                      "properties": {
                        "id": {
                          "type": "string",
                          "column": "AlbumId",
                          "table": "Album",
                          "description": "The unique identifier for an album"
                        },
                        "title": {
                          "type": "string",
                          "column": "Title",
                          "table": "Album",
                          "description": "The title of the album"
                        },
                        "artist": {
                          "type": "object",
                          "column": "ArtistId",
                          "table": "Album",
                          "referenceTo": {
                            "targetTable": "Artist",
                            "targetColumn": "ArtistId"
                          },
                          "properties": {
                            "id": {
                              "type": "string",
                              "column": "ArtistId",
                              "table": "Artist",
                              "description": "The unique identifier for an artist"
                            },
                            "name": {
                              "type": "string",
                              "column": "Name",
                              "table": "Artist",
                              "description": "The name of the artist"
                            }
                          }
                        }
                      }
                    },
                    {
                      "$schema": "http://json-schema.org/draft-07/schema#",
                      "type": "object",
                      "title": "Customer",
                      "properties": {
                        "id": {
                          "type": "string",
                          "column": "CustomerId",
                          "table": "Customer",
                          "description": "The unique identifier for a customer"
                        },
                        "firstName": {
                          "type": "string",
                          "column": "FirstName",
                          "table": "Customer",
                          "description": "Customer's first name"
                        },
                        "lastName": {
                          "type": "string",
                          "column": "LastName",
                          "table": "Customer",
                          "description": "Customer's last name"
                        },
                        "contactInfo": {
                          "type": "object",
                          "properties": {
                            "phone": {
                              "type": "string",
                              "column": "Phone",
                              "table": "Customer",
                              "description": "Customer's phone number"
                            },
                            "email": {
                              "type": "string",
                              "column": "Email",
                              "table": "Customer",
                              "description": "Customer's email address"
                            }
                          }
                        },
                        "supportRep": {
                          "type": "object",
                          "column": "SupportRepId",
                          "table": "Customer",
                          "referenceTo": {
                            "targetTable": "Employee",
                            "targetColumn": "EmployeeId"
                          },
                          "properties": {
                            "id": {
                              "type": "string",
                              "column": "EmployeeId",
                              "table": "Employee",
                              "description": "The unique identifier for an employee"
                            },
                            "name": {
                              "type": "string",
                              "column": "FirstName",
                              "table": "Employee",
                              "description": "Employee's first name"
                            }
                          }
                        }
                      }
                    },
                    {
                      "$schema": "http://json-schema.org/draft-07/schema#",
                      "type": "object",
                      "title": "Invoice",
                      "properties": {
                        "id": {
                          "type": "string",
                          "column": "InvoiceId",
                          "table": "Invoice",
                          "description": "The unique identifier for an invoice"
                        },
                        "customer": {
                          "type": "string",
                          "isReference": true,
                          "column": "CustomerId",
                          "table": "Invoice",
                          "referenceTo": {
                            "targetTable": "Customer",
                            "targetColumn": "CustomerId"
                          }
                        },
                        "total": {
                          "type": "number",
                          "column": "Total",
                          "table": "Invoice",
                          "description": "Total amount for the invoice"
                        }
                      }
                    },
                    {
                      "$schema": "http://json-schema.org/draft-07/schema#",
                      "type": "object",
                      "title": "Track",
                      "properties": {
                        "id": {
                          "type": "string",
                          "column": "TrackId",
                          "table": "Track",
                          "description": "The unique identifier for a track"
                        },
                        "name": {
                          "type": "string",
                          "column": "Name",
                          "table": "Track",
                          "description": "The name of the track"
                        },
                        "album": {
                          "type": "string",
                          "isReference": true,
                          "column": "AlbumId",
                          "table": "Track",
                          "referenceTo": {
                            "targetTable": "Album",
                            "targetColumn": "AlbumId"
                          },
                          "projection" : "Name"
                        },
                        "mediaType": {
                          "type": "string",
                          "isReference": true,
                          "column": "MediaTypeId",
                          "projection" : "Name",
                          "table": "Track",
                          "referenceTo": {
                            "targetTable": "MediaType",
                            "targetColumn": "MediaTypeId"
                          }
                        }
                      }
                    },
                    ...
                  ]
                }
                
                """).append("\n");
        sb.append("```").append("\n");
        sb.append("### Instructions").append("\n");
        sb.append("- **All properties must have a `type`, `column`, and `table` fields**. The `column` and the `table` indicates where this values came from on relational database. \n");
        sb.append("- The fields `column` and `table` **must match** the column and table names in the relational database schema\n");
        sb.append("- If the property is reference (like DBRef), set the `isReference` to true\n");
        sb.append("- To de-reference a property, set the `referenceTo` field with the target table and column\n");
        sb.append("- If the property is a composition of another object, set the `properties` field with the object properties\n");
        sb.append("- If the property is an array, set the `items` field with the array items\n");
        sb.append("- If the property references is a foreign key and the type is not 'object', set the `projection` field with the column name to be projected\n");
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
            """, '\n');
        mc.addTitle3("Instructions");
        mc.addListItem("Ensure all fields in the JSON schema are accurately mapped to their corresponding Java data types.");
        mc.addListItem("Include relevant annotations (e.g., @Id, @NotNull, etc.) for the fields where applicable, based on the context provided in the schema.");
        mc.addListItem("The output should strictly follow the given structure.");
        mc.addListItem("If the field type is a custom class (e.g., Address), ensure that the class is also included in the mapping.");



        return mc.toString();
    }

}
