package org.utfpr.mf.stream;

import org.jetbrains.annotations.NotNull;

import java.io.*;

public class FilePrintStream extends MfPrintStream<FileOutputStream> {

    FileOutputStream fileOutputStream;

    public FilePrintStream(String filePath) throws FileNotFoundException {
        super(new FileOutputStream(filePath));
        this.fileOutputStream = (FileOutputStream) out;
    }

    @Override
    public FilePrintStream clean() {
        // TODO: Erase file content
        return this;
    }

    public FileOutputStream get() {
        return fileOutputStream;
    }

}
