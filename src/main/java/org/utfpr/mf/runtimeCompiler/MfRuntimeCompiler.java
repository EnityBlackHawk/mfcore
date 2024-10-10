package org.utfpr.mf.runtimeCompiler;

import org.jetbrains.annotations.Nullable;
import org.springframework.data.mongodb.repository.support.MongoAnnotationProcessor;
import org.utfpr.mf.tools.CodeSession;

import javax.annotation.processing.Processor;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.ToolProvider;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MfRuntimeCompiler extends CodeSession {


    public MfRuntimeCompiler() {
        super("MfRuntimeCompiler");
    }


    public Map<String, Class<?>> compile(Map<String, String> sources, MfCompilerParams compileParams) throws Exception {
        return compile(sources, compileParams, new MfDefaultPreCompileAction());
    }

    public Map<String, Class<?>> compile(Map<String, String> sources, MfCompilerParams compileParams, @Nullable IMfPreCompileAction action) throws Exception {

        System.out.println("Initializing MfRuntimeCompiler");

        BEGIN("Executing pre-compile actions");
        if(action != null) {
            Map<String, String> newSources = new HashMap<>();
            for (String className : sources.keySet()) {
                System.out.println("[MfRuntimeCompiler] Executing pre-compile action for: " + className);
                newSources.put(className, action.action(className, sources.get(className)));
            }
            sources = newSources;
        }
        BEGIN("Creating MfFileManager");
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        MfFileManager fileManager = new MfFileManager(compiler.getStandardFileManager(null, null, null));
        List<JavaFileObject> files = new ArrayList<>();

        for (String className : sources.keySet()) {
            BEGIN_SUB("Creating source for: " + className);
            JavaFileObject sourceObj = new MfSourceFromString(className, sources.get(className));
            files.add(sourceObj);
        }

        BEGIN("Getting classpath");
        var classPath = compileParams.getClasspath();
        Iterable<String> options = List.of("-classpath", classPath, "--add-exports", "jdk.compiler/com.sun.tools.javac.processing=ALL-UNNAMED");

        INFO(" Using classpath: " + classPath);
        BEGIN("Getting Processors:");

        //Class<?> cLombokProcessor = Class.forName("lombok.launch.AnnotationProcessorHider$AnnotationProcessor");
        //Processor lomProcessor = (Processor) cLombokProcessor.getDeclaredConstructor().newInstance();
        Processor mongoAnnotationProcessor = new MongoAnnotationProcessor();
        Iterable<Processor> processors = List.of(mongoAnnotationProcessor);

        for(var x : processors)
        {
            INFO("Using Processor: " + x.getClass().getSimpleName());
        }

        BEGIN("Config compilation");
        JavaCompiler.CompilationTask task = compiler.getTask(null, fileManager, null, options, sources.keySet(), files);
        task.setProcessors(processors);

        BEGIN("Compiling");
        boolean result = task.call();
        if (!result) {
            throw new RuntimeException("Compilation failed.");
        }
        ClassLoader classLoader = fileManager.getClassLoader(null);

        Map<String, Class<?>> classes = new HashMap<>();

        BEGIN("Getting compiled classes");
        for(String className : sources.keySet()) {
            classes.put(className, classLoader.loadClass(className));
        }

        return classes;
    }

    public Class<?> compile(String className, String source, @Nullable MfCompilerParams params) throws Exception {

        return compile(Map.of(className, source), params, new MfDefaultPreCompileAction()).get(className);
    }
}
