package org.utfpr.mf.migration;

import org.utfpr.mf.migration.params.MigrationSpec;
import org.utfpr.mf.mongoConnection.MongoConnectionCredentials;

import java.io.PrintStream;

public class MfMigrationStepFactory {

    private PrintStream printStream;

    public MfMigrationStepFactory() {
        this(System.out);
    }

    public MfMigrationStepFactory(PrintStream printStream) {
        this.printStream = printStream;
    }

    public IMfMigrationStep createAcquireMetadataStep() {
        return new AcquireMetadataStep(printStream);
    }

    public IMfMigrationStep createGenerateModelStep(MigrationSpec migrationSpec) {
        return new GenerateModelStep(migrationSpec, printStream);
    }

    public IMfMigrationStep createGenerateJavaCodeStep() {
        return new GenerateJavaCodeStep(printStream);
    }

    public IMfMigrationStep createMigrateDatabaseStep(MongoConnectionCredentials credentials) {
        return new MigrateDatabaseStep(credentials);
    }

    public IMfMigrationStep createValidatorStep() {
        return new VerificationStep(printStream);
    }

    public void setPrintStream(PrintStream printStream) {
        this.printStream = printStream;
    }
}
