package org.utfpr.mf.migration;

import lombok.Getter;
import org.utfpr.mf.migration.params.MigrationSpec;
import org.utfpr.mf.mongoConnection.MongoConnectionCredentials;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

public class MfMigrationStepFactory {

    @Getter
    private List<IMfMigrationStep> steps = new ArrayList<>();

    private PrintStream printStream;

    public MfMigrationStepFactory() {
        this(System.out);
    }

    public MfMigrationStepFactory(PrintStream printStream) {
        this.printStream = printStream;
    }

    public IMfMigrationStep createAcquireMetadataStep() {
        var tmp = new AcquireMetadataStep(printStream);
        steps.add(tmp);
        return tmp;
    }

    public IMfMigrationStep createGenerateModelStep(MigrationSpec migrationSpec) {
        var tmp = new GenerateModelStep(migrationSpec, printStream);
        steps.add(tmp);
        return tmp;
    }

    public IMfMigrationStep createGenerateJavaCodeStep() {
        var tmp = new GenerateJavaCodeStep(printStream);
        steps.add(tmp);
        return tmp;
    }

    public IMfMigrationStep createMigrateDatabaseStep(MongoConnectionCredentials credentials) {
        var tmp = new MigrateDatabaseStep(credentials);
        steps.add(tmp);
        return tmp;
    }

    public IMfMigrationStep createValidatorStep() {
        var tmp = new VerificationStep(printStream);
        steps.add(tmp);
        return tmp;
    }

    public void setPrintStream(PrintStream printStream) {
        this.printStream = printStream;
    }
}
