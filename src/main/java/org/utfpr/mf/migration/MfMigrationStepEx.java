package org.utfpr.mf.migration;

import org.utfpr.mf.annotation.Export;
import org.utfpr.mf.annotation.State;
import org.utfpr.mf.enums.DefaultInjectParams;
import org.utfpr.mf.exceptions.InvalidData;
import org.utfpr.mf.exceptions.InvalidOutputData;
import org.utfpr.mf.interfaces.IMfBinder;
import org.utfpr.mf.interfaces.IMfMigrationStep;
import org.utfpr.mf.interfaces.IMfStepObserver;
import org.utfpr.mf.tools.CodeSession;
import org.utfpr.mf.tools.MfCacheController;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public abstract class MfMigrationStepEx<TInput, TOutput> extends CodeSession implements IMfMigrationStep {

    protected final Class<TInput> inputType;
    protected final Class<TOutput> outputType;
    protected ArrayList<IMfStepObserver<TInput, TOutput>> observers = new ArrayList<>();
    private boolean stopSign = false;


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
            e.printStackTrace(_printStream);
            notifyCrash(e);
            return null;
        }
        notifyEnd(outputType.cast(result));
        return result;
    }

    protected boolean notifyStart(TInput input) {
        boolean rest = false;
        for(var o : observers) {
            rest = o.OnStepStart(getClassName(), input);
        }
        return rest;
    }

    protected boolean notifyEnd(TOutput output) {
        boolean rest = false;
        for(var o : observers) {
            rest = o.OnStepEnd(getClassName(), output);
        }
        return rest;
    }

    protected boolean notifyUpdate(Object message, Class<?> messageType) {
        boolean result = true;
        for(var o : observers) {
            result &= o.OnUpdate(getClassName(), message, messageType);
        }
        return result;
    }

    protected void notifyCrash(Throwable error) {
        for(var o : observers) {
            o.OnStepCrash(getClassName(), error);
        }
    }

    public boolean notifyError(String message) {
        boolean rest = false;
        for(var o : observers) {
            rest = o.OnStepError(getClassName(), message);
        }
        return rest;
    }

    protected void callStopSign() {
        stopSign = true;
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
    public boolean validateOutput(Object output) {
        if(!hasValidOutput(output)) {
            notifyCrash(new InvalidOutputData(getClass().getName(), output == null ? "null" : output.getClass().getName()));
            ERROR("Invalid output data: " + (output == null ? "null" : output.getClass().getName()));
            return false;
        }
        return true;
    }

    @Override
    public boolean validateInput(Object input) {
        if(!hasValidInput(input)) {
            notifyCrash(new InvalidData(getClass().getName(), input == null ? "null" : input.getClass().getName(), inputType.getName()));
            ERROR("Invalid input data: " + (input == null ? "null" : input.getClass().getName()));
            return false;
        }
        return true;
    }

    @Override
    public boolean wasStopSignCalled() {
        return stopSign;
    }

    @Override
    public void export(IMfBinder binder) {
        var fields = new ArrayList<>(
                  Arrays.stream(getClass().getDeclaredFields()).filter(f -> f.isAnnotationPresent(Export.class)).toList()
        );
        var sup = getClass().getSuperclass();
        while (sup != null) {
            fields.addAll(Arrays.stream(sup.getDeclaredFields()).filter(f -> f.isAnnotationPresent(Export.class)).toList());
            sup = sup.getSuperclass();
        }
        for(var f : fields) {
            var an = f.getAnnotation(Export.class);
            if(!an.override() && binder.has(an.value().getValue())) {
                continue;
            }
            f.setAccessible(true);
            try {
                String key = Objects.equals(an.value().getValue(), DefaultInjectParams.UNSET.getValue()) ? f.getName() : an.value().getValue();
                binder.bind(key, f.get(this));
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public String getState(Object input) {

        var fields = new ArrayList<>(Arrays.stream(getClass().getDeclaredFields()).filter(f -> f.isAnnotationPresent(State.class)).map((x) -> {
            try {
                return x.get(this);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }).toList());

        if(input != null) {
            fields.add(input);
        }

        if(fields.isEmpty()) {
            return "";
        }

        if(fields.size() == 1) {
            return MfCacheController.generateMD5(fields);
        }
        ArrayList<String> mds = new ArrayList<>();
        for(var f : fields) {
           mds.add(MfCacheController.generateMD5(f));
        }

        return MfCacheController.combineMD5(mds);
    }
}
