import org.junit.jupiter.api.Test;
import org.utfpr.mf.runtimeCompiler.MfCompilerParams;
import org.utfpr.mf.runtimeCompiler.MfDefaultPreCompileAction;
import org.utfpr.mf.runtimeCompiler.MfRuntimeCompiler;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MfCompilerTest {

    @Test
    void compileAndRun() throws Exception {

        String source = """
            public class HelloWorld {
                public String hello() {
                    return "Hello, World!";
                }
            }
        """;
        var mrc = new MfRuntimeCompiler();
        Map<String, Class<?>> classList = mrc.compile(
                Map.of("HelloWorld", source),
                MfCompilerParams.builder()
                        .classpathBasePath("/home/luan/jars/")
                        .classPath(List.of("lombok.jar", "spring-data-commons-3.3.4.jar", "spring-data-mongodb-4.3.4.jar"))
                        .build(),
                new MfDefaultPreCompileAction());
        Object instance = classList.get("HelloWorld").getDeclaredConstructor().newInstance();
        String value = (String) classList.get("HelloWorld").getMethod("hello").invoke(instance);
        assertEquals("Hello, World!", value);

    }

}
