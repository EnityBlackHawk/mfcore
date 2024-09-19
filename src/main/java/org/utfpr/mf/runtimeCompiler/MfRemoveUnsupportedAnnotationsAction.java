package org.mf.langchain.runtimeCompiler;

import javax.annotation.Nullable;

public class MfRemoveUnsupportedAnnotationsAction implements IMfPreCompileAction {

    private IMfPreCompileAction _next;

    public MfRemoveUnsupportedAnnotationsAction(@Nullable IMfPreCompileAction next) {
        this._next = next;
    }

    @Override
    public String action(String className, String source) {
        var result = source.replaceAll("@Document.*(?:\\\\r?\\\\n|\\\\r)?", "");
        return this._next != null ? this._next.action(className, result) : result;
    }

    @Override
    public void setNext(IMfPreCompileAction next) {
        this._next = next;
    }


}
