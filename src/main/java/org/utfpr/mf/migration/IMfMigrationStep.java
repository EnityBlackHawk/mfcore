package org.utfpr.mf.migration;

public interface IMfMigrationStep {

    Object execute(Object input);
    boolean hasValidOutput(Object selfOutput);
    boolean hasValidInput(Object input);

}
