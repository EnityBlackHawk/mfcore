import org.junit.jupiter.api.Test;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.utfpr.mf.MockLayer;
import org.utfpr.mf.enums.DefaultInjectParams;
import org.utfpr.mf.metadata.DbMetadata;
import org.utfpr.mf.migration.*;
import org.utfpr.mf.migration.params.*;
import org.utfpr.mf.mongoConnection.MongoConnection;
import org.utfpr.mf.mongoConnection.MongoConnectionCredentials;
import org.utfpr.mf.prompt.Framework;
import org.utfpr.mf.runtimeCompiler.MfCompilerParams;
import org.utfpr.mf.runtimeCompiler.MfDefaultPreCompileAction;
import org.utfpr.mf.runtimeCompiler.MfRuntimeCompiler;
import org.utfpr.mf.tools.ConvertToJavaFile;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class MfMigratorTest {

//    @Test
//    public void MigrationAll() {
//
//        MockLayer.isActivated = true;
//
//        final boolean[] valuesStart = { false, false, false, false, false };
//        final boolean[] valuesEnd = { false, false, false, false, false };
//
//        IMfStepObserver observer = new IMfStepObserver() {
//
//            static int index;
//
//            @Override
//            public boolean OnStepStart(String stepName, Object o) {
//                valuesStart[index] = true;
//                return true;
//            }
//
//            @Override
//            public boolean OnStepEnd(String stepName, Object o) {
//                valuesEnd[index++] = true;
//                return true;
//            }
//
//            @Override
//            public boolean OnStepCrash(String stepName, Throwable error) {
//                return true;
//            }
//
//            @Override
//            public boolean OnStepError(String stepName, String message) {
//                return true;
//            }
//        };
//
//        MigrationSpec migrationSpec = MigrationSpec.builder()
//                .LLM("gpt-4o-mini")
//                .allow_ref(true)
//                .name("test")
//                .prioritize_performance(true)
//                .framework(Framework.SPRING_DATA)
//                .build();
//
//        MongoConnectionCredentials credentials = new MongoConnectionCredentials("localhost", 27017, "test_2",null, null);
//        MongoConnection mc  = new MongoConnection(credentials);
//        mc.getTemplate().getCollectionNames().stream().forEach(
//                (n) -> mc.getTemplate().dropCollection(n)
//        );
//
//
//        MfMigrationStepFactory mfMigrationStepFactory = new MfMigrationStepFactory();
//
//        IMfMigrationStep acquireMetadataStep = mfMigrationStepFactory.createAcquireMetadataStep(observer);
//        IMfMigrationStep generateModelStep = mfMigrationStepFactory.createGenerateModelStep(migrationSpec, observer);
//        IMfMigrationStep generateJavaCodeStep = mfMigrationStepFactory.createGenerateJavaCodeStep(observer);
//        IMfMigrationStep migrateDatabaseStep = mfMigrationStepFactory.createMigrateDatabaseStep(credentials, observer);
//        IMfMigrationStep validatorStep = mfMigrationStepFactory.createValidatorStep(observer);
//
//        IMfBinder binder = new MfMigrator.Binder()
//                .bind(DefaultInjectParams.LLM_KEY, System.getenv("LLM_KEY"));
//
//        MfMigrator mfMigrator = new MfMigrator(binder, List.of(acquireMetadataStep, generateModelStep, generateJavaCodeStep, migrateDatabaseStep, validatorStep));
//        var cred = new RdbCredentials("jdbc:postgresql://localhost/airport3", "admin", "admin");
//        VerificationReport rep = (VerificationReport) mfMigrator.execute(cred);
//
//        assertTrue(rep.isCountTestSucceeded(), () -> "Count test failed: " + rep.getCountTestMessage());
//        assertTrue(rep.isHashTestSucceeded(), () -> "Hash test failed: " + rep.getHashTestMessage());
//        assertArrayEquals(new boolean[] {true, true, true, true, true}, valuesStart);
//        assertArrayEquals(new boolean[] {true, true, true, true, true}, valuesEnd);
//    }
//
//    @Test
//    public void GenerateJavaCode() throws Exception {
//        MockLayer.isActivated = true;
//
//        IMfBinder binder = new MfMigrator.Binder()
//                .bind(DefaultInjectParams.LLM_KEY, System.getenv("LLM_KEY"));
//
//        final boolean[] values = { false, false };
//
//        IMfStepObserver<Model, GeneratedJavaCode> observer = new IMfStepObserver<Model, GeneratedJavaCode>() {
//            @Override
//            public boolean OnStepStart(String stepName, Model model) {
//                values[0] = true;
//                return true;
//            }
//
//            @Override
//            public boolean OnStepEnd(String stepName, GeneratedJavaCode generatedJavaCode) {
//                values[1] = true;
//                return true;
//            }
//
//            @Override
//            public boolean OnStepCrash(String stepName, Throwable error) {
//                return true;
//            }
//
//            @Override
//            public boolean OnStepError(String stepName, String message) {
//                return true;
//            }
//        };
//
//        String result = GenerateModelStep.MOCK_GENERATE_MODEL;
//        ArrayList<String> objs = new ArrayList<>();
//        GenerateModelStep.extracted(result, objs);
//
//        Model model = new Model( objs.stream().reduce("", String::concat), 0 );
//
//        MfMigrationStepFactory mfMigrationStepFactory = new MfMigrationStepFactory();
//
//        IMfMigrationStep generateJavaCodeStep = mfMigrationStepFactory.createGenerateJavaCodeStep(observer);
//
//        MfMigrator migrator = new MfMigrator(binder, List.of(generateJavaCodeStep));
//        GeneratedJavaCode res = (GeneratedJavaCode) migrator.execute(model);
//
//        var params = MfCompilerParams.builder().classpathBasePath("/home/luan/jars/")
//                .classPath(List.of("lombok.jar", "spring-data-commons-3.3.4.jar", "spring-data-mongodb-4.3.4.jar"))
//                .build();
//        var compiler = new MfRuntimeCompiler();
//        var classes = compiler.compile(res.getCode(), params);
//
//        assertNotNull(classes);
//        assertArrayEquals(new boolean[] {true, true}, values);
//    }
//
//
//    @Test
//    public void ValidatorStepTest() throws SQLException {
//        MockLayer.isActivated = true;
//
//        var sources = ConvertToJavaFile.toMap(GenerateJavaCodeStep.MOCK_RESPONSE);
//        MfRuntimeCompiler compiler = new MfRuntimeCompiler();
//        MfCompilerParams params = MfCompilerParams.builder()
//                .classpathBasePath("/home/luan/jars/")
//                .classPath(List.of("lombok.jar", "spring-data-commons-3.3.4.jar", "spring-data-mongodb-4.3.4.jar"))
//                .build();
//        Map<String, Class<?>> compiledClasses = new HashMap<>();
//        try {
//            compiledClasses = compiler.compile(sources, params, new MfDefaultPreCompileAction());
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//        assert !compiledClasses.isEmpty() : "No classes were compiled";
//
//        var fac = new MfMigrationStepFactory();
//        var valStep = fac.createValidatorStep();
//
//        IMfBinder binder = new MfMigrator.Binder()
//                .bind(DefaultInjectParams.DB_METADATA, new DbMetadata("jdbc:postgresql://localhost/airport3", "admin", "admin", null))
//                .bind(DefaultInjectParams.MONGO_CONNECTION.getValue(), new MongoConnection(new MongoConnectionCredentials("localhost", 27017, "test_2", null, null)));
//
//        MfMigrator migrator = new MfMigrator(binder, List.of(valStep));
//        VerificationReport rep = (VerificationReport) migrator.execute(new MigrationDatabaseReport(Map.of(), compiledClasses));
//
//        assertTrue(rep.isCountTestSucceeded(), () -> "Count test failed: " + rep.getCountTestMessage());
//        assertTrue(rep.isHashTestSucceeded(), () -> "Hash test failed: " + rep.getHashTestMessage());
//
//    }

}
