package org.utfpr.mf.migration;

import org.utfpr.mf.annotarion.Injected;
import org.utfpr.mf.enums.DefaultInjectParams;
import org.utfpr.mf.exceptions.InvalidData;
import org.utfpr.mf.tools.CodeSession;

import java.io.PrintStream;
import java.util.*;

public class MfMigrator extends CodeSession {

    private ArrayList<IMfMigrationStep> steps = new ArrayList<>();
    private Binder binder;

    public static class Binder {
        private final Map<String, Object> bindings = new HashMap<>();

        public Binder bind(DefaultInjectParams key, Object value) {
            return bind(key.getValue(), value);
        }

        public Binder bind(String key, Object value) {
            bindings.put(key, value);
            return this;
        }

        private void inject(Object target) {
//            for (Map.Entry<String, Object> entry : bindings.entrySet()) {
//                String key = entry.getKey();
//                Object value = entry.getValue();
//                try {
//                    var f = target.getClass().getField(key);
//                    f.setAccessible(true);
//                    f.set(target, value);
//                } catch (IllegalAccessException | NoSuchFieldException e) {
//                    throw new RuntimeException(e);
//                }
//            }
            Arrays.stream(target.getClass().getDeclaredFields()).filter(f -> f.isAnnotationPresent(Injected.class)).forEach(f -> {
                var an = f.getAnnotation(Injected.class);
                f.setAccessible(true);
                try {
                    f.set(target, bindings.get(an.value() == DefaultInjectParams.UNSET ? f.getName() : an.value().getValue()));
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            });

        }

    }

    public MfMigrator(Binder binder, List<IMfMigrationStep> steps) {
        this(binder, steps, System.out);
    }

    public MfMigrator(Binder binder, List<IMfMigrationStep> steps, PrintStream printStream) {
        super("MfMigrator", printStream);
        this.steps.addAll(steps);
        this.binder = binder;
    }

    public void addStep(IMfMigrationStep step) {
        steps.add(step);
    }

    public Object execute(Object firstInput) {

        BEGIN("Injecting values");
        for (var x : steps) {
            binder.inject(x);
        }

        BEGIN("Executing steps");
        Object lastOutput = firstInput;
        for (var x : steps) {
            if(!x.hasValidInput(lastOutput)) {
                throw new InvalidData(x.getClass().getName(), lastOutput.getClass().getName());
            }
            lastOutput = x.execute(lastOutput);
            assert x.hasValidOutput(lastOutput) : "Step " + x.getClass().getSimpleName() + " has invalid output: " + lastOutput.getClass().getName();
        }

        return lastOutput;
    }


}
