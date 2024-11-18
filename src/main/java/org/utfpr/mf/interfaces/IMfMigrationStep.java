package org.utfpr.mf.interfaces;

public interface IMfMigrationStep{

    Object execute(Object input);
    void export(IMfBinder binder);
    boolean hasValidOutput(Object selfOutput);
    boolean hasValidInput(Object input);
    boolean validateOutput(Object output);
    boolean validateInput(Object input);
    boolean wasStopSignCalled();
    String getState(Object input);

}
