package org.utfpr.mf.stream;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.OutputStream;
import java.io.PrintStream;

public abstract class MfPrintStream<T> extends PrintStream {


    public MfPrintStream(@NotNull OutputStream out) {
        super(out);
    }

    @Override
    public void println(@Nullable String x) {
        super.print(x + '\n');
    }

    @Override
    public void println(@Nullable Object x) {
        super.println( (x != null ? x.toString() : "null") + '\n');
    }

    public abstract MfPrintStream clean();
    public abstract T get();
}
