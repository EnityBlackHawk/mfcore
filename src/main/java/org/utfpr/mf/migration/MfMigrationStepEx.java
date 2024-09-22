package org.utfpr.mf.migration;

import org.utfpr.mf.tools.CodeSession;

import java.io.PrintStream;

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
        return outputType.isInstance(selfOutput);
    }

    @Override
    public boolean hasValidInput(Object input) {
        return inputType.isInstance(input);
    }

}
