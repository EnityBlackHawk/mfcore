package org.utfpr.mf.migration;

import org.utfpr.mf.annotarion.Export;
import org.utfpr.mf.tools.CodeSession;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

public abstract class MfMigrationStepEx<TInput, TOutput> extends CodeSession implements IMfMigrationStep{

    protected final Class<TInput> inputType;
    protected final Class<TOutput> outputType;
    protected ArrayList<IMfStepObserver<TInput, TOutput>> observers = new ArrayList<>();


    public MfMigrationStepEx(String className, Class<TInput> inputType, Class<TOutput> outputType) {
        super(className);
        this.inputType = inputType;
        this.outputType = outputType;
    }

    public MfMigrationStepEx(String className, PrintStream printStream, Class<TInput> inputType, Class<TOutput> outputType) {
        super(className, printStream);
        this.inputType = inputType;
        this.outputType = outputType;
    }


    public void addObserver(IMfStepObserver<TInput, TOutput> observer) {
        observers.add(observer);
    }

    public List<IMfStepObserver<TInput, TOutput>> getObservers() {
        return observers;
    }

    protected Object executeHelper(Function<TInput, TOutput> func, Object input) {
        TInput castedInput = inputType.cast(input);
        notifyStart(castedInput);
        Object result = null;
        try {
            result = func.apply(castedInput);
        }catch (Throwable e) {
            ERROR(e.getMessage());
            notifyCrash(e);
            return null;
        }
        notifyEnd(outputType.cast(result));
        return result;
    }

    protected boolean notifyStart(TInput input) {
        boolean rest = true;
        for(var o : observers) {
            rest = o.OnStepStart(getClassName(), input);
        }
        return rest;
    }

    protected boolean notifyEnd(TOutput output) {
        boolean rest = true;
        for(var o : observers) {
            rest = o.OnStepEnd(getClassName(), output);
        }
        return rest;
    }

    protected void notifyCrash(Throwable error) {
        for(var o : observers) {
            o.OnStepCrash(getClassName(), error);
        }
    }

    protected boolean notifyError(String message) {
        boolean rest = true;
        for(var o : observers) {
            rest = o.OnStepError(getClassName(), message);
        }
        return rest;
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
