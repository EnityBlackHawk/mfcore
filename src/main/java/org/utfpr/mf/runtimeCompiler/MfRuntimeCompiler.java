package org.mf.langchain.runtimeCompiler;

import io.hypersistence.utils.common.ClassLoaderUtils;

import javax.annotation.Nullable;
import javax.annotation.processing.Processor;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.ToolProvider;
import java.io.File;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URLClassLoader;
import java.util.*;
import lombok.launch.*;
import org.springframework.data.mongodb.repository.support.MongoAnnotationProcessor;

public class MfRuntimeCompiler {

    public static Map<String, Class<?>> compile(Map<String, String> sources, @Nullable IMfPreCompileAction action) throws Exception {

        System.out.println("Initializing MfRuntimeCompiler");

        System.out.println("[MfRuntimeCompiler] Executing pre-compile actions");
        if(action != null) {
            Map<String, String> newSources = new HashMap<>();
            for (String className : sources.keySet()) {
                System.out.println("[MfRuntimeCompiler] Executing pre-compile action for: " + className);
                newSources.put(className, action.action(className, sources.get(className)));
            }
            sources.clear();
            sources = newSources;
        }
        System.out.println("[MfRuntimeCompiler] Creating MfFileManager");
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        MfFileManager fileManager = new MfFileManager(compiler.getStandardFileManager(null, null, null));
        List<JavaFileObject> files = new ArrayList<>();

        for (String className : sources.keySet()) {
            System.out.println("[MfRuntimeCompiler] Creating source for: " + className);
            JavaFileObject sourceObj = new MfSourceFromString(className, sources.get(className));
            files.add(sourceObj);
        }

        var classPath = System.getProperty("java.class.path") +
                File.pathSeparator + "/home/luan/jars/lombok.jar" +
                File.pathSeparator + "/home/luan/jars/spring-data-commons-3.3.4.jar" +
                File.pathSeparator + "/home/luan/jars/spring-data-mongodb-4.3.4.jar";
        Iterable<String> options = List.of("-classpath", classPath, "--add-exports", "jdk.compiler/com.sun.tools.javac.processing=ALL-UNNAMED");

        System.out.println("[MfRuntimeCompiler] Using classpath: " + classPath);
        System.out.println("[MfRuntimeCompiler] Getting Processors:");

        //Class<?> cLombokProcessor = Class.forName("lombok.launch.AnnotationProcessorHider$AnnotationProcessor");
        //Processor lomProcessor = (Processor) cLombokProcessor.getDeclaredConstructor().newInstance();
        Processor mongoAnnotationProcessor = new MongoAnnotationProcessor();
        Iterable<Processor> processors = List.of(mongoAnnotationProcessor);

        for(var x : processors)
        {
            System.out.println("[MfRuntimeCompiler] Using Processor: " + x.getClass().getSimpleName());
        }

        System.out.println("[MfRuntimeCompiler] Config compilation");
        JavaCompiler.CompilationTask task = compiler.getTask(null, fileManager, null, options, sources.keySet(), files);
        task.setProcessors(processors);

        System.out.println("[MfRuntimeCompiler] Compiling");
        boolean result = task.call();
        if (!result) {
            throw new RuntimeException("Compilation failed.");
        }
        ClassLoader classLoader = fileManager.getClassLoader(null);

        Map<String, Class<?>> classes = new HashMap<>();

        System.out.println("[MfRuntimeCompiler] Getting compiled classes");
        for(String className : sources.keySet()) {
            classes.put(className, classLoader.loadClass(className));
        }

        return classes;
    }

    public static Class<?> compile(String className, String source) throws Exception {

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        MfFileManager fileManager = new MfFileManager(compiler.getStandardFileManager(null, null, null));

        JavaFileObject sourceObj = new MfSourceFromString(className, source);
        List<JavaFileObject> files = List.of(sourceObj);

        var classPath = System.getProperty("java.class.path") + File.pathSeparator + "/home/luan/jars/lombok.jar";
        Iterable<String> options = List.of("-classpath", "/home/luan/jars/lombok.jar");

        JavaCompiler.CompilationTask task = compiler.getTask(null, fileManager, null, options, null, files);
        boolean result = task.call();
        if (!result) {
            throw new RuntimeException("Compilation failed.");
        }
        ClassLoader classLoader = fileManager.getClassLoader(null);
        return classLoader.loadClass(className);
    }
}
