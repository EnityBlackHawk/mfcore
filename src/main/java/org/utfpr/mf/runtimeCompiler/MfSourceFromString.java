package org.mf.langchain.runtimeCompiler;

import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import java.net.URI;

public class MfSourceFromString extends SimpleJavaFileObject {

    private final String source;

    public MfSourceFromString(String className, String source)
    {
        super(URI.create("string:///" + className.replace('.', '/') + JavaFileObject.Kind.SOURCE.extension), JavaFileObject.Kind.SOURCE);
        this.source = source;
    }

    @Override
    public CharSequence getCharContent(boolean ignoreEncodingErrors)
    {
        return source;
    }

}
