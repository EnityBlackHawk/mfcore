import org.junit.jupiter.api.Test;
import org.utfpr.mf.MockLayer;
import org.utfpr.mf.enums.DefaultInjectParams;
import org.utfpr.mf.migration.IMfMigrationStep;
import org.utfpr.mf.migration.MfMigrationStepFactory;
import org.utfpr.mf.migration.MfMigrator;
import org.utfpr.mf.migration.params.MigrationSpec;
import org.utfpr.mf.migration.params.RdbCredentials;
import org.utfpr.mf.prompt.Framework;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class MfMigratorTest {

    @Test
    public void AcquireMetadataStep() {

        MockLayer.isActivated = true;

        MigrationSpec migrationSpec = MigrationSpec.builder()
                .LLM("gpt-4o-mini")
                .allow_ref(true)
                .name("test")
                .prioritize_performance(true)
                .framework(Framework.SPRING_DATA)
                .build();

        MfMigrationStepFactory mfMigrationStepFactory = new MfMigrationStepFactory();
        IMfMigrationStep acquireMetadataStep = mfMigrationStepFactory.createAcquireMetadataStep();
        IMfMigrationStep generateModelStep = mfMigrationStepFactory.createGenerateModelStep(migrationSpec);
        assertNotNull(acquireMetadataStep);

        MfMigrator.Binder binder = new MfMigrator.Binder()
                .bind(DefaultInjectParams.LLM_KEY, System.getenv("LLM_KEY"));

        MfMigrator mfMigrator = new MfMigrator(binder, List.of(acquireMetadataStep, generateModelStep));
        var cred = new RdbCredentials("jdbc:postgresql://localhost/airport3", "admin", "admin");
        var result = mfMigrator.execute(cred);

        assertEquals("Model", result.getClass().getSimpleName());

    }

}
