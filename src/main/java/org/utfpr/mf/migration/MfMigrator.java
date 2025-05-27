package org.utfpr.mf.migration;

import org.utfpr.mf.descriptor.MfMigratorDesc;
import org.utfpr.mf.enums.DefaultInjectParams;
import org.utfpr.mf.interfaces.IMfBinder;
import org.utfpr.mf.interfaces.IMfMigrationStep;
import org.utfpr.mf.llm.LLMService;
import org.utfpr.mf.tools.CodeSession;
import org.utfpr.mf.tools.TemplatedThread;

import java.io.PrintStream;
import java.util.*;

public class MfMigrator extends CodeSession {

    private ArrayList<IMfMigrationStep> steps = new ArrayList<>();
    private IMfBinder binder;
    private LLMService llmService;

    public static class Binder extends MfBinderEx {

        public MfBinderEx bind(DefaultInjectParams key, Object value) {
            return bind(key.getValue(), value);
        }

    }

    public MfMigrator(MfMigratorDesc desc) {
        super("MfMigrator", desc.printStream);
        this.steps = new ArrayList<>(desc.steps);
        this.binder = desc.binder;
        this.llmService = new LLMService(desc.llmServiceDesc);

        this.binder.bind( DefaultInjectParams.LLM_SERVICE.getValue(), this.llmService );

    }

    public MfMigrator(IMfBinder binder, List<IMfMigrationStep> steps, LLMService llmService, PrintStream printStream) {
        super("MfMigrator", printStream);
        this.steps.addAll(steps);
        this.llmService = llmService;
        this.binder = binder;
        this.binder.bind( DefaultInjectParams.LLM_SERVICE.getValue(), this.llmService );
    }

    public void addStep(IMfMigrationStep step) {
        steps.add(step);
    }
    public void clearSteps() {
        steps.clear();
    }
    public void setSteps(List<IMfMigrationStep> steps) {
        this.steps = new ArrayList<>(steps);
    }

    public TemplatedThread<Object> executeAsync(Object firstInput) {

        var thread = new TemplatedThread<Object>(() -> execute(firstInput));
        thread.runAsync();
        return thread;
    }


    public Object execute(Object firstInput) {

        Object lastOutput = firstInput;
        for (var x : steps) {
            BEGIN("Executing steps " + x.getClass().getSimpleName());
            BEGIN_SUB("Injecting dependencies");
            binder.inject(x);

            if(!x.validateInput(lastOutput)) {
                break;
            }

            lastOutput = x.execute(lastOutput);

            if(x.wasStopSignCalled()) {
                INFO("Stop called by: " + x.getClass().getSimpleName());
            }

            if(!x.validateOutput(lastOutput)) {
                break;
            }
            x.export(binder);
        }

        return lastOutput;
    }


}
