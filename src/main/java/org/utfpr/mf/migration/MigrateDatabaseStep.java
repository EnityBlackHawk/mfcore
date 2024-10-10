package org.utfpr.mf.migration;


import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.utfpr.mf.annotarion.Export;
import org.utfpr.mf.annotarion.Injected;
import org.utfpr.mf.enums.DefaultInjectParams;
import org.utfpr.mf.metadata.DbMetadata;
import org.utfpr.mf.migration.params.GeneratedJavaCode;
import org.utfpr.mf.migration.params.MigrationDatabaseReport;
import org.utfpr.mf.mongoConnection.MongoConnection;
import org.utfpr.mf.mongoConnection.MongoConnectionCredentials;
import org.utfpr.mf.runtimeCompiler.*;
import org.utfpr.mf.tools.DataImporter;
import org.utfpr.mf.tools.QueryResult;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MigrateDatabaseStep extends MfMigrationStepEx<GeneratedJavaCode, MigrationDatabaseReport> {

    @Getter
    @Setter
    private MongoConnectionCredentials mongoConnectionCredentials;

    @Injected(DefaultInjectParams.MONGO_CONNECTION)
    @Export(DefaultInjectParams.MONGO_CONNECTION)
    private MongoConnection mongoConnection;

    @Injected(DefaultInjectParams.DB_METADATA)
    @Export(DefaultInjectParams.DB_METADATA)
    private DbMetadata dbMetadata = null;

    public MigrateDatabaseStep(MongoConnectionCredentials connectionCredentials) {
        this(connectionCredentials, System.out);
    }

    public MigrateDatabaseStep(MongoConnectionCredentials connectionCredentials, PrintStream printStream) {
        super("MigrateDatabaseStep", printStream, GeneratedJavaCode.class, MigrationDatabaseReport.class);
        this.mongoConnectionCredentials = connectionCredentials;
    }

    private MigrationDatabaseReport process(GeneratedJavaCode generatedJavaCode) {
        assert dbMetadata != null : "dbMetadata is not set";

        BEGIN("Initializing MfRuntimeCompiler");
        MfRuntimeCompiler compiler = new MfRuntimeCompiler();
        MfCompilerParams params = MfCompilerParams.builder()
                .classpathBasePath("/home/luan/jars/")
                .classPath(List.of("lombok.jar", "spring-data-commons-3.3.4.jar", "spring-data-mongodb-4.3.4.jar"))
                .build();
        Map<String, Class<?>> compiledClasses = new HashMap<>();
        IMfPreCompileAction action = new MfDefaultPreCompileAction( new MfVerifyImportAction());
        try {
            compiledClasses = compiler.compile(generatedJavaCode.getCode(), params, action);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        assert !compiledClasses.isEmpty() : "No classes were compiled";

        BEGIN("Connecting to MongoDB");
        if(mongoConnection == null) {
            mongoConnection = new MongoConnection(mongoConnectionCredentials);
        }
        else {
            INFO("MongoConnection already set, using existing");
        }
        BEGIN("Migrating data");
        var counts = makeMigration(dbMetadata, mongoConnection, compiledClasses);
        return new MigrationDatabaseReport(counts, compiledClasses);
    }


    @Override
    public Object execute(Object input) {
        return executeHelper(this::process, input);
    }

    private Map<String, Integer> makeMigration(DbMetadata dbMetadata, MongoConnection mongoConnection, Map<String, Class<?>> classes) {
        HashMap<String, Integer> classCount = new HashMap<>();
        for(String className : classes.keySet())
        {
            BEGIN_SUB("Querying data from " + className);
            QueryResult qr = DataImporter.Companion.runQuery(String.format("SELECT * FROM %s", className), dbMetadata, QueryResult.class);
            BEGIN_SUB("Converting data: " + className);
            List<?> objects = qr.asObject(classes.get(className));
            BEGIN_SUB("Persisting data: " + className);
            MongoTemplate mTemplate = mongoConnection.getTemplate();
            int count = 0;
            for(var obj : objects)
            {
                mTemplate.insert(obj);
                count++;
            }
            classCount.put(className, count);
        }
        return classCount;
    }
}
