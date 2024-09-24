package org.utfpr.mf.tools;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class ConvertToJavaFile {


    public static String getFromFile(String path) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(path + "cache.txt"));
            return reader.lines().collect(Collectors.joining(System.lineSeparator()));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static void saveToFile(String path, String content) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(path + "cache.txt"));
            writer.write(content);
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Map<String, String> toMap(String content) {
        var contents = new ArrayList<String>();
        while(true){
            var start = content.indexOf("```java");
            if(start == -1)
                break;
            var start_2 = content.substring(start + 8);
            var end = start_2.indexOf("```");
            contents.add(start_2.substring(0, end));
            content = content.substring(start + 8 + end);
        }

        var map = new HashMap<String, String>();
        for(String c : contents){
            var classNameIndex = c.indexOf("class");
            var isInterface = classNameIndex == -1;
            if(isInterface) {
                continue;
            }
            var className = c.substring(classNameIndex + 6, c.indexOf("{")).trim();
            map.put(className, c);
        }

        return map;
    }

    public static void toFile(String path, String _package, String content) {


        var contents = new ArrayList<String>();
        while(true){
            var start = content.indexOf("```java");
            if(start == -1)
                break;
            var start_2 = content.substring(start + 8);
            var end = start_2.indexOf("```");
            contents.add(start_2.substring(0, end));
            content = content.substring(start + 8 + end);
        }

        for(String c : contents){
            var classNameIndex = c.indexOf("class");
            var isInterface = classNameIndex == -1;
            if(isInterface) {
                classNameIndex = c.indexOf("interface");
            }
            var className = c.substring(classNameIndex + (isInterface ? 9 : 6), c.indexOf( isInterface ? "extends" : "{")).trim();
            try {
                BufferedWriter writer = new BufferedWriter(new FileWriter(path + className + ".java"));
                writer.write("package " + _package + ";\n\n");
                writer.write(c);
                writer.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }

    }
}
