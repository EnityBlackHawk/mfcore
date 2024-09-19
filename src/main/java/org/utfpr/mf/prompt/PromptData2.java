package org.mf.langchain.prompt;

import org.jetbrains.annotations.Nullable;
import org.mf.langchain.metadata.DbMetadata;
import org.springframework.data.util.Pair;

import java.util.List;

public class PromptData2 extends PromptData {

    private final Boolean useMarkdown;
    private final String cardinalityTable;

    public PromptData2(DbMetadata dbMetadata, MigrationPreferences migrationPreference, Boolean allowReferences, Framework framework, @Nullable String cardinalityTable, Boolean useMarkdown, List<Query> queryList, List<String> remarks) {
        super(dbMetadata, migrationPreference, allowReferences, framework, queryList, remarks);
        this.useMarkdown = useMarkdown;
        this.cardinalityTable = cardinalityTable;
        this.maxCalls = 1;
        if(cardinalityTable != null) {
            customPrompts.add("Use the relationships cardinality info to model the database");
        }
    }

    public PromptData2(String sqlTables, MigrationPreferences migrationPreference, Boolean allowReferences, Framework framework, @Nullable String cardinalityTable, Boolean useMarkdown, List<Query> queryList, List<String> customPrompts) {
        super(sqlTables, migrationPreference, allowReferences, framework, queryList, customPrompts);
        this.useMarkdown = useMarkdown;
        this.cardinalityTable = cardinalityTable;
        if(cardinalityTable != null) {
            customPrompts.add("Use the relationships cardinality info to model the database");
        }
    }

    @Override
    public String get() {

        StringBuilder sb = new StringBuilder();
        var infos = this.getSqlTablesAndQueries();

        sb.append("# For this relational database: \n")
                .append("```sql\n")
                .append(infos.getFirst())
                .append("```\n");
        if(!queryList.isEmpty())
        {
            sb.append("## Consider these commonly used queries when modeling the MongoDB database: \n");
            sb.append(infos.getSecond());
            sb.append("\n");
        }
        if(cardinalityTable != null)
        {
            sb.append("## Relationships Cardinality: \n");
            sb.append("```json \n").append(cardinalityTable).append("\n``` \n");
            sb.append("\n");
        }

        if(!customPrompts.isEmpty()){
            sb.append("## Modeling remarks: \n");
            for (var x : customPrompts) {
                sb.append("- ").append(x).append("\n");
            }
            if(userDefinedPrompts != null)
                for(var x : userDefinedPrompts) {
                    sb.append("- ").append(x).append("\n");
                }
            sb.append("\n");
        }

//        if(!customCodePrompts.isEmpty()){
//            sb.append("## Generated code remarks: \n");
//            for(var x : customCodePrompts) {
//                sb.append("- ").append(x).append("\n");
//            }
//            sb.append("\n");
//        }
        sb.append("Your task: Suggest a MongoDB structure following the provided information.");

        var result = sb.toString();
        return useMarkdown ? result : removeMarkdown(result);
    }

    private String removeMarkdown(String s) {
        return s.replaceAll("(```sql|```|'|-|##|#)", "");
    }

    @Override
    public Pair<String, String> getSqlTablesAndQueries() {
        String s = "";
        if(dbMetadata != null) {
            for (var x : dbMetadata.getTables()) {
                s = s.concat(x.toString() + "\n");
            }
        }
        else {
            s = sqlTables;
        }

        String q = "";
        for(var x : queryList) {
            q = q.concat("- " + (x.regularity() != null ? "**Used " + x.regularity() + "% of the time**: " : "") + "`" + x.query() + "`\n");
        }
        return Pair.of(s, q);
    }


    @Override
    public String next() {
        return get();
    }
}
