package org.utfpr.mf.runtimeCompiler;

import javax.tools.SimpleJavaFileObject;
import java.net.URI;

public class MfSourceFromString extends SimpleJavaFileObject {

    private final String source;

    public MfSourceFromString(String className, String source)
    {
        super(URI.create("string:///" + className.replace('.', '/') + Kind.SOURCE.extension), Kind.SOURCE);
        this.source = source;
    }

    @Override
    public CharSequence getCharContent(boolean ignoreEncodingErrors)
    {
        return source;
    }

}
