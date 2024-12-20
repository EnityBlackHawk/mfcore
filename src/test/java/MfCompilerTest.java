import org.junit.jupiter.api.Test;
import org.utfpr.mf.runtimeCompiler.MfCompilerParams;
import org.utfpr.mf.runtimeCompiler.MfDefaultPreCompileAction;
import org.utfpr.mf.runtimeCompiler.MfRuntimeCompiler;
import org.utfpr.mf.runtimeCompiler.MfVerifyImportAction;
import org.utfpr.mf.stream.StringPrintStream;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MfCompilerTest {

    @Test
    void loadJars() throws Exception {

        MfRuntimeCompiler.loadResourceJars();

    }

    @Test
    void compileAndRun() throws Exception {

        String source = """
       public class HelloWorld {
           public static class Test {
            private LocalDateTime date;
           }
       
           public String hello() {
               return "Hello, World!";
           }
       }
       """;

        String mongoPOJO = """
                import lombok.AllArgsConstructor;
                import lombok.Data;
                import org.springframework.data.annotation.Id;
                
                @Data
                @AllArgsConstructor
                public class DocumentTest {
                    @Id
                    private String id;
                    private String name;
                
                }
                """;



        var mrc = new MfRuntimeCompiler();
        Map<String, Class<?>> classList = mrc.compile(
                Map.of("HelloWorld", source, "DocumentTest", mongoPOJO),
                MfCompilerParams.builder()
                        .classPath(MfRuntimeCompiler.loadResourceJars("lombok.jar", "spring-data-commons-3.3.4.jar", "spring-data-mongodb-4.3.4.jar"))
                        .build(),
                new MfDefaultPreCompileAction(
                        new MfVerifyImportAction()
                ));
        Object instance = classList.get("HelloWorld").getDeclaredConstructor().newInstance();
        String value = (String) classList.get("HelloWorld").getMethod("hello").invoke(instance);
        assertEquals("Hello, World!", value);

    }
}
