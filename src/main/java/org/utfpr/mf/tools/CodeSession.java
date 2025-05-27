package org.utfpr.mf.tools;

import java.io.PrintStream;

public class CodeSession implements ISession {

    private final String _className;
    public static PrintStream LastSet;
    protected final PrintStream _printStream;

    public CodeSession(String className) {
        this(className, System.out);
    }

    public CodeSession(String className, PrintStream printStream) {
        this._className = className;
        _printStream = printStream;
        LastSet = _printStream;
    }

    @Override
    public PrintStream getPrintStream() {
        return _printStream;
    }

    @Override
    public void BEGIN(String sessionName) {
        ISession.super.BEGIN(sessionName);
    }

    @Override
    public String getClassName() {
        return this._className;
    }
}
