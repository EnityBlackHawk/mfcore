import org.junit.jupiter.api.Test;
import org.utfpr.mf.interfaces.IMfBinder;
import org.utfpr.mf.migration.AcquireMetadataStep;
import org.utfpr.mf.migration.MfMigrator;
import org.utfpr.mf.migration.params.MigrationSpec;
import org.utfpr.mf.prompt.Framework;
import org.utfpr.mf.tools.MfCacheController;

import java.security.NoSuchAlgorithmException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class CacheControllerTest {

    @Test
    void md5() {
        MigrationSpec spec1 = MigrationSpec.builder()
                .LLM("gpt-4o-mini")
                .name("teste")
                .allow_ref(true)
                .framework(Framework.SPRING_DATA)
                .prioritize_performance(true)
                .build();

        MigrationSpec spec2 = MigrationSpec.builder()
                .LLM("gpt-4o-mini")
                .name("teste")
                .allow_ref(true)
                .framework(Framework.SPRING_DATA)
                .prioritize_performance(true)
                .build();

        assertEquals(MfCacheController.generateMD5(spec1), MfCacheController.generateMD5(spec2));

        spec2.setPrioritize_performance(false);
        assertNotEquals(MfCacheController.generateMD5(spec1), MfCacheController.generateMD5(spec2));
    }
}
