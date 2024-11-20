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

    @Override
    public String getFirst() {
        return super.getFirst();
    }

    public static String getSecond(String jsonDocuments, @Nullable Framework framework) {

        MarkdownContent mc = new MarkdownContent();
        mc.addCodeBlock(jsonDocuments, "json");
        mc.addPlainText("Analyze the provided JSON schema and generate a mapping in the following format:", '\n');
        mc.addCodeBlock("""
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
                """, "json");
        mc.addPlainText("Ensure all fields in the JSON schema are accurately mapped to their corresponding Java data types. " +
                "Additionally, include relevant annotations (e.g., @Id, @NotNull, etc.) for the fields where applicable, based on the context provided in the schema. " +
                "The output should strictly follow the given structure.", '\0');


        return mc.toString();
    }

}
