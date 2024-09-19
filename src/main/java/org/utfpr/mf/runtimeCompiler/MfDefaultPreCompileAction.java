package org.mf.langchain.runtimeCompiler;

import javax.naming.OperationNotSupportedException;

public class MfDefaultPreCompileAction implements IMfPreCompileAction {

    private final IMfPreCompileAction _next;

    public MfDefaultPreCompileAction() {
        this(null);
    }

    public MfDefaultPreCompileAction(IMfPreCompileAction next) {
        this._next = new MfRemoveUnsupportedAnnotationsAction(next);
    }

    @Override
    public String action(String className, String source) {
        return this._next.action(className, source);
    }

    @Override
    public void setNext(IMfPreCompileAction next) throws OperationNotSupportedException {
        throw new OperationNotSupportedException("Cannot decorate the default pre-compile action");
    }
}
