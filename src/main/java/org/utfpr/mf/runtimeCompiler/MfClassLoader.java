package org.mf.langchain.runtimeCompiler;

public class MfClassLoader extends ClassLoader{

    private final MfFileManager manager;

    public MfClassLoader(ClassLoader parent, MfFileManager manager) {
        super(parent);
        this.manager = manager;
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        var compiledClasses = manager.getCompiledClasses();
        if(compiledClasses.containsKey(name))
        {
            byte[] bytes = compiledClasses.get(name).getBytes();
            return defineClass(name, bytes, 0, bytes.length);
        }
        else
        {
            throw new ClassNotFoundException();
        }
    }

}
