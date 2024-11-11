package org.utfpr.mf.runtimeCompiler;

import lombok.Builder;
import lombok.Data;

import java.io.File;
import java.util.List;

@Data
@Builder
public class MfCompilerParams {

    private String classpathBasePath;
    private List<String> classPath;


    public String getClasspath() {
        StringBuilder sb = new StringBuilder();
        sb.append(System.getProperty("java.class.path"));

        if(classPath == null) {
            return "";
        }

        for (String path : classPath) {
            sb.append(File.pathSeparator).append(classpathBasePath != null ? classpathBasePath : "");
            sb.append(path);
        }
        return sb.toString();
    }
}
