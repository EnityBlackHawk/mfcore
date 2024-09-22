import org.junit.jupiter.api.Test;
import org.utfpr.mf.migration.IMfMigrationStep;
import org.utfpr.mf.migration.MfMigrationStepFactory;
import org.utfpr.mf.migration.MfMigrator;
import org.utfpr.mf.migration.params.RdbCredentials;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class MfMigratorTest {

    @Test
    public void AcquireMetadataStep() {
        MfMigrationStepFactory mfMigrationStepFactory = new MfMigrationStepFactory();
        IMfMigrationStep acquireMetadataStep = mfMigrationStepFactory.createAcquireMetadataStep();
        assertNotNull(acquireMetadataStep);

        MfMigrator.Binder binder = new MfMigrator.Binder()
                .bind("llm_key", System.getenv("LLM_KEY"));

        MfMigrator mfMigrator = new MfMigrator(binder, List.of(acquireMetadataStep));
        var cred = new RdbCredentials("jdbc:postgresql://localhost/airport3", "admin", "admin");
        var result = mfMigrator.execute(cred);

        assertEquals("MetadataInfo", result.getClass().getSimpleName());

    }

}
