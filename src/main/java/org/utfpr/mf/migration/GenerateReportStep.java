package org.utfpr.mf.migration;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.utfpr.mf.annotation.Injected;
import org.utfpr.mf.enums.DefaultInjectParams;
import org.utfpr.mf.json.JsonSchemaList;
import org.utfpr.mf.llm.LLMService;
import org.utfpr.mf.markdown.MarkdownContent;
import org.utfpr.mf.markdown.MarkdownDocument;
import org.utfpr.mf.metadata.DbMetadata;
import org.utfpr.mf.migration.params.MigrationDatabaseReport;
import org.utfpr.mf.prompt.Query;
import org.utfpr.mf.prompt.desc.PromptData4Desc;
import org.utfpr.mf.tools.QueryResult;
import org.utfpr.mf.tools.QueryResult2;

import java.io.PrintStream;
import java.util.Date;
import java.util.Map;
import java.util.Objects;

public class GenerateReportStep extends MfMigrationStepEx<MigrationDatabaseReport, MarkdownContent>{




    @Injected(value = DefaultInjectParams.REPORT_PATH, required = false)
    private String reportPath;

    @Injected(DefaultInjectParams.UNSET)
    private String llm_explanation;

    @Injected(DefaultInjectParams.UNSET)
    private PromptData4Desc promptData4Desc;

    @Injected(DefaultInjectParams.LLM_SERVICE)
    private LLMService llmService;

    @Injected(DefaultInjectParams.JSON_SCHEMA_LIST)
    private JsonSchemaList recipes;


    public GenerateReportStep() {
        this(System.out);
    }

    public GenerateReportStep(PrintStream printStream) {
        super("Generate Report", printStream, MigrationDatabaseReport.class, MarkdownContent.class);
    }

    private MarkdownContent process(MigrationDatabaseReport input) {
        MarkdownContent content = new MarkdownContent();

        content.addTitle1("Migration Report");
        content.addPlainText("**Date:** " + new Date(), '\n');

        content.addTitle2("Inputs:");

        QueryResult inputs = new QueryResult2("Parameter", "Value")
                .addRow("allowReferences", promptData4Desc.allowReferences.toString())
                .addRow("framework", promptData4Desc.framework.toString())
                .addRow("referenceOnly", promptData4Desc.referenceOnly.toString())
                .addRow("useMarkdown", promptData4Desc.useMarkdown.toString())
                .addRow("migrationPreference", promptData4Desc.migrationPreference.toString());

        content.addTable(inputs);

        content.addTitle3("Relational Schema");
        content.addCodeBlock(promptData4Desc.sqlTables, "sql");

        content.addTitle3("Cardinality Information");
        content.addCodeBlock(Objects.requireNonNullElse(promptData4Desc.cardinalityTable, ""), "json");

        if(promptData4Desc.queryList != null && !promptData4Desc.queryList.isEmpty()) {
            content.addTitle3("Workload");
            for (Query query : promptData4Desc.queryList) {
                content.addListItem(String.format("**Used %s%% of the time:**", query.regularity()));
                content.addCodeBlock(query.query(), "sql");
                content.addPlainText("", '\n');
            }
        }


        content.addTitle2("Model generation");
        content.addPlainText("**LLM Model:** " + llmService.getDescription().model, '\n');

        content.addTitle3("LLM Explanation");
        content.addPlainText(llm_explanation, '\n');


        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        content.addPlainText("""
                <details>
                <summary>
                """, '\n');
        content.addTitle3("JSON Schema");
        content.addPlainText("\n</summary>\n", '\n');
        content.addCodeBlock(gson.toJson(recipes), "json");
        content.addPlainText("\n</details>\n", '\n');

        if(input != null) {
            content.addTitle2("Migration Database Report");
            QueryResult2 qr = new QueryResult2("Class", "Count");
            for (Map.Entry<String, Integer> entry : input.getTables_count().entrySet()) {
                qr.addRow(entry.getKey(), entry.getValue().toString());
            }
            content.addTitle3("Tables Count");
            content.addTable(qr);
        }

        if(reportPath != null) {
            MarkdownDocument md = new MarkdownDocument(reportPath );
            md.write(content);
        }

        return content;

    }


    @Override
    public Object execute(Object input) {
        return executeHelper(this::process, input);
    }
}
