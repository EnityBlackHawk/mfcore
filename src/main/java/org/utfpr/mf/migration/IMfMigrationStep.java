package org.utfpr.mf.migration;

public interface IMfMigrationStep{

    Object execute(Object input);
    void export(IMfBinder binder);
    boolean hasValidOutput(Object selfOutput);
    boolean hasValidInput(Object input);
    boolean validateOutput(Object output);
    boolean validateInput(Object input);

}
