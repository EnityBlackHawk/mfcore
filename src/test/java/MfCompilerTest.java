import org.junit.jupiter.api.Test;
import org.utfpr.mf.runtimeCompiler.MfDefaultPreCompileAction;
import org.utfpr.mf.runtimeCompiler.MfRuntimeCompiler;

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

        Map<String, Class<?>> classList = MfRuntimeCompiler.compile(
                Map.of("HelloWorld", source),
                new MfDefaultPreCompileAction()
                );
        Object instance = classList.get("HelloWorld").getDeclaredConstructor().newInstance();
        String value = (String) classList.get("HelloWorld").getMethod("hello").invoke(instance);
        assertEquals("Hello, World!", value);

    }

}
