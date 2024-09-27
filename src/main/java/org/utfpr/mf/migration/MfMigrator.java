package org.utfpr.mf.migration;

import org.utfpr.mf.annotarion.Injected;
import org.utfpr.mf.enums.DefaultInjectParams;
import org.utfpr.mf.exceptions.InvalidData;
import org.utfpr.mf.tools.CodeSession;

import java.io.PrintStream;
import java.util.*;

public class MfMigrator extends CodeSession {

    private ArrayList<IMfMigrationStep> steps = new ArrayList<>();
    private IMfBinder binder;

    public static class Binder extends MfBinderEx {

        public MfBinderEx bind(DefaultInjectParams key, Object value) {
            return bind(key.getValue(), value);
        }

    }

    public MfMigrator(IMfBinder binder, List<IMfMigrationStep> steps) {
        this(binder, steps, System.out);
    }

    public MfMigrator(IMfBinder binder, List<IMfMigrationStep> steps, PrintStream printStream) {
        super("MfMigrator", printStream);
        this.steps.addAll(steps);
        this.binder = binder;
    }

    public void addStep(IMfMigrationStep step) {
        steps.add(step);
    }

    public Object execute(Object firstInput) {


        Object lastOutput = firstInput;
        for (var x : steps) {
            BEGIN("Executing steps " + x.getClass().getSimpleName());
            BEGIN_SUB("Injecting dependencies");
            binder.inject(x);

            if(!x.hasValidInput(lastOutput)) {
                throw new InvalidData(x.getClass().getName(), lastOutput.getClass().getName());
            }
            lastOutput = x.execute(lastOutput);
            if(!x.hasValidOutput(lastOutput)) {
                throw new RuntimeException("Step " + x.getClass().getSimpleName() + " has invalid output: " + lastOutput.getClass().getName());
            }
            x.export(binder);
        }

        return lastOutput;
    }


}
