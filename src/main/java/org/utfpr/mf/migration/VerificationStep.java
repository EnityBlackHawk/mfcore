package org.utfpr.mf.migration;

import org.springframework.data.annotation.Id;
import org.utfpr.mf.annotation.Injected;
import org.utfpr.mf.enums.BasicClasses;
import org.utfpr.mf.enums.DefaultInjectParams;
import org.utfpr.mf.metadata.DbMetadata;
import org.utfpr.mf.metadata.GenericRegistry;
import org.utfpr.mf.metadata.Table;
import org.utfpr.mf.migration.params.MigrationDatabaseReport;
import org.utfpr.mf.migration.params.VerificationReport;
import org.utfpr.mf.mongoConnection.MongoConnection;
import org.utfpr.mf.tools.DataImporter;
import org.utfpr.mf.tools.QueryResult;

import java.io.PrintStream;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class VerificationStep extends MfMigrationStepEx<MigrationDatabaseReport, VerificationReport> {

    @Injected(DefaultInjectParams.DB_METADATA)
    private DbMetadata dbMetadata = null;

    @Injected(DefaultInjectParams.MONGO_CONNECTION)
    private MongoConnection mongoConnection = null;

    public VerificationStep() {
        this(System.out);
    }


    public VerificationStep(PrintStream printStream) {
        super("VerificationStep", printStream, MigrationDatabaseReport.class, VerificationReport.class);
    }

    private VerificationReport process(MigrationDatabaseReport report) {

        var verificationBuilder = VerificationReport.builder();

        BEGIN("Verifying database connection");
        if (mongoConnection == null) {
            throw new RuntimeException("MongoConnection is not set");
        }
        if(!dbMetadata.isConnected()) {
            throw new RuntimeException("Database connection is not established");
        }

        BEGIN("Acquiring data from MongoDB and RDB");
        for(Class<?> source : report.getSources().values()) {

            BEGIN("Verifying data from " + source.getSimpleName());
            List<?> resultMongo = mongoConnection.getTemplate().findAll(source);
            assert !resultMongo.isEmpty() : "No data found for " + source.getName();

            Table table = dbMetadata.getTables().stream().filter(t -> t.name().equals(source.getSimpleName().toLowerCase())).findFirst().orElse(null);
            if(table == null) {
                throw new RuntimeException("Table " + source.getSimpleName() + " not found in database");
            }
            var queryResult = DataImporter.Companion.runQuery("SELECT * FROM " + table.name(), dbMetadata, QueryResult.class);
            List<GenericRegistry> resultRdb = queryResult.asGenericRegistry();

            BEGIN_SUB("Analysing sizes");
            if(resultMongo.size() != resultRdb.size()) {
                String msg = "[" + source.getSimpleName() + "] Different sizes detected: " + resultMongo.size() + " in MongoDB and " + resultRdb.size() + " in RDB";
                INFO(msg);
                verificationBuilder.isCountTestSucceeded(false);
                verificationBuilder.countTestMessage(msg);
            }
            else {
                verificationBuilder.isCountTestSucceeded(true);
            }

            BEGIN_SUB("Hashing data");
            List<String> md5Mongo = resultMongo.stream().map(r -> {
                try {
                    return generateMd5(r);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e.getMessage());
                }
            }).toList();

            List<String> md5Rdb = resultRdb.stream().map(GenericRegistry::generateMD5).toList();
            BEGIN_SUB("Beginning comparison");
            verificationBuilder.isHashTestSucceeded(true);
            for(int i = 0; i < md5Mongo.size(); i++) {
                if(!md5Mongo.get(i).equals(md5Rdb.get(i))) {
                    String msg = "[" + source.getSimpleName() + "] Different data detected in row " + i;
                    INFO(msg);
                    verificationBuilder.isHashTestSucceeded(false);
                    verificationBuilder.hashTestMessage(msg);
                    break;
                }
            }

        }

        return verificationBuilder.build();
    }

    @Override
    public Object execute(Object input) {
        return executeHelper(this::process, input);
    }

    public String generateMd5(Object data) throws IllegalAccessException {
        String concat = "";
        var fields = data.getClass().getDeclaredFields();
        fields = Arrays.stream(fields).sorted(Comparator.comparing(Field::getName)).toArray(Field[]::new);
        for(var f : fields) {
            f.setAccessible(true);
            var clazz = f.getType();
            if(clazz.isPrimitive() || BasicClasses.isBasicClass(clazz.getSimpleName())) {
                concat = concat.concat(f.get(data).toString());
                continue;
            }

            Object value = f.get(data);
            if(value == null) {
                continue;
            }
            var vField = Arrays.stream(value.getClass().getDeclaredFields())
                    .filter(v -> v.isAnnotationPresent(Id.class)).findFirst().orElse(
                            Arrays.stream(value.getClass().getDeclaredFields())
                                    .filter(v -> v.getName().toLowerCase(Locale.ROOT).contains("id"))
                                    .findFirst().orElse(null)
                    );
            if (vField == null) {
                throw new RuntimeException("No @Id field found in " + value.getClass().getName());
            }
            vField.setAccessible(true);
            concat = concat.concat(vField.get(value).toString());
        }

        return org.apache.commons.codec.digest.DigestUtils.md5Hex(concat);
    }

    private boolean compareDataMd5(String a, Object b) {
        return false;
    }
}
