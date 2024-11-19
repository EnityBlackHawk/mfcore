package org.utfpr.mf.descriptor;

import org.utfpr.mf.interfaces.IMfBinder;
import org.utfpr.mf.interfaces.IMfMigrationStep;
import org.utfpr.mf.migration.MfMigrator;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

public class MfMigratorDesc {

    public List<IMfMigrationStep> steps;
    public IMfBinder binder = new MfMigrator.Binder();
    public LLMServiceDesc llmServiceDesc = new LLMServiceDesc();
    public PrintStream printStream = System.out;

}
