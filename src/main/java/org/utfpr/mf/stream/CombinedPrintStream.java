package org.utfpr.mf.stream;


import org.jetbrains.annotations.Nullable;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;

public class CombinedPrintStream extends MfPrintStream<String> {

    private ArrayList<MfPrintStream<?>> printStreams = new ArrayList<>();
    private MfPrintStream<?> main;

    public CombinedPrintStream(MfPrintStream main, MfPrintStream<?>... seconds) {
        super(main);
        printStreams.addAll(Arrays.stream(seconds).toList());
        this.main = main;
    }

    @Override
    public CombinedPrintStream clean() {
        main.clean();
        for (MfPrintStream<?> printStream : printStreams) {
            printStream.clean();
        }
        return this;
    }

    @Override
    public String get() {
        return main.get().toString();
    }

    @Override
    public void print(@Nullable Object obj) {
        super.print(obj);
        for (PrintStream printStream : printStreams) {
            printStream.print(obj);
        }
    }

    @Override
    public void println(@Nullable Object x) {
        super.println(x);
        for (PrintStream printStream : printStreams) {
            printStream.println(x);
        }
    }

    @Override
    public void print(@Nullable String s) {
        main.println(s);
        for (MfPrintStream printStream : printStreams) {
             printStream.println(s);
        }
    }

    @Override
    public void println(@Nullable String x) {
        super.println(x);
        for (PrintStream printStream : printStreams) {
            printStream.println(x);
        }
    }


}
