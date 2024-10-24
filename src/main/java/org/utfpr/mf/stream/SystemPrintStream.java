package org.utfpr.mf.stream;

import org.jetbrains.annotations.NotNull;

import java.io.OutputStream;
import java.io.PrintStream;

public class SystemPrintStream extends MfPrintStream<PrintStream> {

    public SystemPrintStream() {
        super(System.out);
    }

    @Override
    public SystemPrintStream clean() {
        return this;
    }

    @Override
    public PrintStream get() {
        return System.out;
    }
}
