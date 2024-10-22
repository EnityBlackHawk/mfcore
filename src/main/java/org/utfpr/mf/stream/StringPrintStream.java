package org.utfpr.mf.stream;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class StringPrintStream extends PrintStream {

    private ByteArrayOutputStream byteArray;

    public StringPrintStream() {
        super(new ByteArrayOutputStream());
        byteArray = (ByteArrayOutputStream) out;
    }

    public String get() {
        return byteArray.toString();
    }

    @Override
    public String toString() {
        return get();
    }
}
