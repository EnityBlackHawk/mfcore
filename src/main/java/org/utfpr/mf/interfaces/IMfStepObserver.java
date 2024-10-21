package org.utfpr.mf.interfaces;

public interface IMfStepObserver<TInput, TOutput> {

    boolean OnStepStart(String stepName, TInput input);
    boolean OnStepEnd(String stepName, TOutput output);
    boolean OnStepCrash(String stepName, Throwable error);
    boolean OnStepError(String stepName, String message);
}
