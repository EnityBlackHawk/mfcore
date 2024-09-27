package org.utfpr.mf.migration;

import org.utfpr.mf.annotarion.Export;
import org.utfpr.mf.tools.CodeSession;

import java.io.PrintStream;
import java.util.Arrays;

public abstract class MfMigrationStepEx extends CodeSession implements IMfMigrationStep {

    protected final Class<?> inputType;
    protected final Class<?> outputType;


    public MfMigrationStepEx(String className, Class<?> inputType, Class<?> outputType) {
        super(className);
        this.inputType = inputType;
        this.outputType = outputType;
    }

    public MfMigrationStepEx(String className, PrintStream printStream, Class<?> inputType, Class<?> outputType) {
        super(className, printStream);
        this.inputType = inputType;
        this.outputType = outputType;
    }

    @Override
    public boolean hasValidOutput(Object selfOutput) {
        return selfOutput == outputType || outputType.isInstance(selfOutput);
    }

    @Override
    public boolean hasValidInput(Object input) {
        return inputType == null || inputType == input || inputType.isInstance(input);
    }

    @Override
    public void export(IMfBinder binder) {
        var fields = Arrays.stream(getClass().getDeclaredFields()).filter(f -> f.isAnnotationPresent(Export.class)).toList();
        for(var f : fields) {
            var an = f.getAnnotation(Export.class);
            if(!an.override() && binder.has(an.value().getValue())) {
                continue;
            }
            f.setAccessible(true);
            try {
                binder.bind(an.value().getValue(), f.get(this));
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
