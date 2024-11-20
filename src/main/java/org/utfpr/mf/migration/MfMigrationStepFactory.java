package org.utfpr.mf.migration;

import lombok.Getter;
import org.utfpr.mf.interfaces.IMfMigrationStep;
import org.utfpr.mf.interfaces.IMfStepObserver;
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

    public IMfMigrationStep createAcquireMetadataStep(IMfStepObserver... observer) {
        var tmp = new AcquireMetadataStep(printStream);
        for (var o : observer) {
            tmp.addObserver(o);
        }
        steps.add(tmp);
        return tmp;
    }

    public IMfMigrationStep createGenerateModelStep(IMfStepObserver... observers) {
        var tmp = new GenerateModelStep(null, printStream);
        for (var o : observers) {
            tmp.addObserver(o);
        }
        steps.add(tmp);
        return tmp;
    }

    public IMfMigrationStep createGenerateModelStep(MigrationSpec migrationSpec, IMfStepObserver... observers) {
        var tmp = new GenerateModelStep(migrationSpec, printStream);
        for (var o : observers) {
            tmp.addObserver(o);
        }
        steps.add(tmp);
        return tmp;
    }

    public IMfMigrationStep createGenerateJavaCodeStep(IMfStepObserver... observers) {
        var tmp = new GenerateJavaCodeStep(printStream);
        for (var o : observers) {
            tmp.addObserver(o);
        }
        steps.add(tmp);
        return tmp;
    }

    public IMfMigrationStep createGenerateJavaCodeStep2(IMfStepObserver... observers) {
        var tmp = new GenerateJavaCodeStep2(printStream);
        for (var o : observers) {
            tmp.addObserver(o);
        }
        steps.add(tmp);
        return tmp;
    }

    public IMfMigrationStep createMigrateDatabaseStep(IMfStepObserver... observers) {
        var tmp = new MigrateDatabaseStep(printStream);
        for (var o : observers) {
            tmp.addObserver(o);
        }
        steps.add(tmp);
        return tmp;
    }

    public IMfMigrationStep createMigrateDatabaseStep(MongoConnectionCredentials credentials, IMfStepObserver... observers) {
        var tmp = new MigrateDatabaseStep(credentials, printStream);
        for (var o : observers) {
            tmp.addObserver(o);
        }
        steps.add(tmp);
        return tmp;
    }

    public IMfMigrationStep createValidatorStep(IMfStepObserver... observers) {
        var tmp = new VerificationStep(printStream);
        for (var o : observers) {
            tmp.addObserver(o);
        }
        steps.add(tmp);
        return tmp;
    }

    public IMfMigrationStep createBenchmarkStep(IMfStepObserver... observers) {
        var tmp = new BenchmarkStep(printStream);
        for (var o : observers) {
            tmp.addObserver(o);
        }
        steps.add(tmp);
        return tmp;
    }

    public void setPrintStream(PrintStream printStream) {
        this.printStream = printStream;
    }
}
