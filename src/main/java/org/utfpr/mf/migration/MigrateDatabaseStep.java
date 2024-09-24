package org.utfpr.mf.migration;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.utfpr.mf.annotarion.Injected;
import org.utfpr.mf.enums.DefaultInjectParams;
import org.utfpr.mf.metadata.DbMetadata;
import org.utfpr.mf.migration.params.GeneratedJavaCode;
import org.utfpr.mf.mongoConnection.MongoConnection;
import org.utfpr.mf.mongoConnection.MongoConnectionCredentials;
import org.utfpr.mf.runtimeCompiler.MfCompilerParams;
import org.utfpr.mf.runtimeCompiler.MfDefaultPreCompileAction;
import org.utfpr.mf.runtimeCompiler.MfRuntimeCompiler;
import org.utfpr.mf.tools.DataImporter;
import org.utfpr.mf.tools.QueryResult;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MigrateDatabaseStep extends MfMigrationStepEx {

    @Getter
    @Setter
    private MongoConnectionCredentials mongoConnectionCredentials;

    @Injected(DefaultInjectParams.DB_METADATA)
    private DbMetadata dbMetadata;

    public MigrateDatabaseStep(MongoConnectionCredentials connectionCredentials) {
        this(connectionCredentials, System.out);
    }

    public MigrateDatabaseStep(MongoConnectionCredentials connectionCredentials, PrintStream printStream) {
        super("MigrateDatabaseStep", printStream, GeneratedJavaCode.class, null);
        this.mongoConnectionCredentials = connectionCredentials;
    }

    @Override
    public Object execute(Object input) {
        assert dbMetadata != null : "dbMetadata is not set";
        GeneratedJavaCode generatedJavaCode = (GeneratedJavaCode) input;
        BEGIN("Initializing MfRuntimeCompiler");
        MfRuntimeCompiler compiler = new MfRuntimeCompiler();
        MfCompilerParams params = MfCompilerParams.builder()
                .classpathBasePath("/home/luan/jars/")
                .classPath(List.of("lombok.jar", "spring-data-commons-3.3.4.jar", "spring-data-mongodb-4.3.4.jar"))
                .build();
        Map<String, Class<?>> compiledClasses = new HashMap<>();
        try {
            compiledClasses = compiler.compile(generatedJavaCode.getCode(), params, new MfDefaultPreCompileAction());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        assert !compiledClasses.isEmpty() : "No classes were compiled";

        BEGIN("Connecting to MongoDB");
        MongoConnection connection = new MongoConnection(mongoConnectionCredentials);
        BEGIN("Migrating data");
        makeMigration(dbMetadata, connection, compiledClasses);
        return null;
    }

    private void makeMigration(DbMetadata dbMetadata, MongoConnection mongoConnection, Map<String, Class<?>> classes) {
        for(String className : classes.keySet())
        {
            BEGIN_SUB("Querying data from " + className);
            QueryResult qr = DataImporter.Companion.runQuery(String.format("SELECT * FROM %s", className), dbMetadata, QueryResult.class);
            BEGIN_SUB("Converting data: " + className);
            List<?> objects = qr.asObject(classes.get(className));
            BEGIN_SUB("Persisting data: " + className);
            MongoTemplate mTemplate = mongoConnection.getTemplate();

            for(var obj : objects)
            {
                mTemplate.insert(obj);
            }

        }
    }
}
