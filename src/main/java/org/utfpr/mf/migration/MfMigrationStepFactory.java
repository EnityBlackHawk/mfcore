package org.utfpr.mf.migration;

import lombok.Getter;
import org.utfpr.mf.interfaces.IMfMigrationStep;
import org.utfpr.mf.interfaces.IMfStepObserver;
import org.utfpr.mf.migration.params.GeneratedJavaCode;
import org.utfpr.mf.migration.params.MetadataInfo;
import org.utfpr.mf.migration.params.MigrationSpec;
import org.utfpr.mf.migration.params.Model;
import org.utfpr.mf.mongoConnection.MongoConnectionCredentials;
import org.utfpr.mf.scorus.ScorusStep;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

public class MfMigrationStepFactory {


    public static final Integer CURRENT_VERSION = 2;

    @Getter
    private List<IMfMigrationStep> steps = new ArrayList<>();

    private PrintStream printStream;
    private Integer version;

    public MfMigrationStepFactory() {
        this(System.out);
    }

    public MfMigrationStepFactory(PrintStream printStream) {
        this(CURRENT_VERSION, printStream);
    }

    public MfMigrationStepFactory(Integer version) {
        this(version, System.out);
    }

    public MfMigrationStepFactory(Integer version, PrintStream printStream) {
        this.version = version;
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
        MfMigrationStepEx<MetadataInfo, Model> tmp = switch (version) {
            case 1 -> new GenerateModelStep(printStream);
            default -> new GenerateModelStep2(printStream);
        };

        for (var o : observers) {
            tmp.addObserver(o);
        }
        steps.add(tmp);
        return tmp;
    }

    public IMfMigrationStep createScorusStep(IMfStepObserver... observers) {
        var tmp = new ScorusStep(printStream);
        for (var o : observers) {
            tmp.addObserver(o);
        }
        steps.add(tmp);
        return tmp;
    }

    public IMfMigrationStep createGenerateJavaCodeStep(IMfStepObserver... observers) {
        var tmp = switch (version) {
            case 1 -> new GenerateJavaCodeStep(printStream);
            default -> new GenerateJavaCodeStep2(printStream);
        };
        for (var o : observers) {
            tmp.addObserver(o);
        }
        steps.add(tmp);
        return tmp;
    }


    public IMfMigrationStep createMigrateDatabaseStep(IMfStepObserver... observers) {
        var tmp = switch (version) {
            case 1 -> new MigrateDatabaseStep(printStream);
            default -> new MigrateDatabaseStep2(printStream);
        };
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
