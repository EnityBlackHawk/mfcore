package org.mf.langchain.runtimeCompiler;

import javax.naming.OperationNotSupportedException;

public interface IMfPreCompileAction {
    String action(String className, String source);
    void setNext(IMfPreCompileAction next) throws OperationNotSupportedException;
}
