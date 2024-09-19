package org.mf.langchain.runtimeCompiler;

import javax.tools.SimpleJavaFileObject;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.net.URI;

public class MfClassBytes extends SimpleJavaFileObject {

    protected ByteArrayOutputStream bos = new ByteArrayOutputStream();

    public MfClassBytes(String name, Kind kind) {
        super(URI.create("string:///" + name.replace('.', '/') + kind.extension), kind);
    }

    public byte[] getBytes() {
        return bos.toByteArray();
    }

    @Override
    public OutputStream openOutputStream() {
        return bos;
    }
}
