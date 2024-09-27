package org.utfpr.mf.migration;

public interface IMfStepObserver {

    boolean OnStepStart(String stepName, Object input);
    boolean OnStepEnd(String stepName, Object output);
    void OnStepCrash(String stepName, Throwable error);
    boolean OnStepError(String stepName, String message);
}
