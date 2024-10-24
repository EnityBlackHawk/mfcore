package org.utfpr.mf.stream;

import org.jetbrains.annotations.NotNull;

import java.io.OutputStream;
import java.io.PrintStream;

public abstract class MfPrintStream<T> extends PrintStream {


    public MfPrintStream(@NotNull OutputStream out) {
        super(out);
    }

    public abstract MfPrintStream clean();
    public abstract T get();
}
