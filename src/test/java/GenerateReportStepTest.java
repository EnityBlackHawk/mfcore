import org.junit.jupiter.api.Test;
import org.utfpr.mf.descriptor.LLMServiceDesc;
import org.utfpr.mf.enums.DefaultInjectParams;
import org.utfpr.mf.interfaces.IMfMigrationStep;
import org.utfpr.mf.json.JsonSchemaList;
import org.utfpr.mf.llm.LLMService;
import org.utfpr.mf.migration.MfBinderEx;
import org.utfpr.mf.migration.MfMigrationStepFactory;
import org.utfpr.mf.migration.MfMigrator;
import org.utfpr.mf.migration.params.MigrationDatabaseReport;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class GenerateReportStepTest {


    @Test
    void testGenerateReportStep() throws IOException {

        MfMigrationStepFactory factory = new MfMigrationStepFactory();
        IMfMigrationStep step = factory.createGenerateReportStep();

        MfMigrator.Binder binder = new MfMigrator.Binder();
        binder.bind(DefaultInjectParams.REPORT_PATH, "report.md");
        binder.bind("llm_explanation", "Explicacao de LLM");
        binder.bind("prompt", "Prompt de teste");

        LLMServiceDesc desc = new LLMServiceDesc();
        desc.model = "gpt-4o-mini";
        desc.llm_key = "key";
        binder.bind(DefaultInjectParams.LLM_SERVICE, new LLMService(desc));
        binder.bind(DefaultInjectParams.JSON_SCHEMA_LIST, new JsonSchemaList());

        binder.inject(step);

        MigrationDatabaseReport report = new MigrationDatabaseReport(
                Map.of("ClassA", 10, "ClassB", 20),
                Map.of()
        );

        step.execute(report);

        assertTrue( Files.exists(Path.of("report.md")) );
        assertTrue(Files.readString(Path.of("report.md")).contains("Migration Report"));
        assertTrue(Path.of("report.md").toFile().delete());
    }

}
