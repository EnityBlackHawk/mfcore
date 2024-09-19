package org.mf.langchain.runtimeCompiler;

import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import java.util.Map;

public class MfFileManager extends ForwardingJavaFileManager<JavaFileManager> {

    private final Map<String, MfClassBytes> compiledClasses;
    private ClassLoader loader;

    public MfFileManager(JavaFileManager fileManager) {
        super(fileManager);
        compiledClasses = new java.util.HashMap<>();
        this.loader = new MfClassLoader(this.getClass().getClassLoader(), this);
    }

    @Override
    public JavaFileObject getJavaFileForOutput(Location location, String className, JavaFileObject.Kind kind, FileObject sibling) {

        MfClassBytes classBytes = new MfClassBytes(className, kind);
        compiledClasses.put(className, classBytes);
        return classBytes;
    }

    public Map<String, MfClassBytes> getCompiledClasses() {
        return compiledClasses;
    }

    @Override
    public ClassLoader getClassLoader(Location location) {
        return loader;
    }



}
