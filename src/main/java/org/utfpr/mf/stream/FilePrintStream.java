package org.utfpr.mf.stream;

import org.jetbrains.annotations.NotNull;

import java.io.*;

public class FilePrintStream extends PrintStream {

    FileOutputStream fileOutputStream;

    public FilePrintStream(String filePath) throws FileNotFoundException {
        super(new FileOutputStream(filePath));
        this.fileOutputStream = (FileOutputStream) out;
    }

    public FileOutputStream get() {
        return fileOutputStream;
    }

}
