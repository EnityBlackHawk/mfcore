import com.google.gson.Gson;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.utfpr.mf.json.JsonSchemaList;
import org.utfpr.mf.orms.ORMTable;
import org.utfpr.mf.prompt.Framework;
import org.utfpr.mf.reflection.ClassMetadata;
import org.utfpr.mf.reflection.ClassMetadataList;
import org.utfpr.mf.reflection.JsonSchemaToClassConverter;
import org.utfpr.mf.reflection.MfClassGenerator;
import org.utfpr.mf.runtimeCompiler.MfRuntimeCompiler;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class JsonSchemaToClassConverterTest {

    private static JsonSchemaList schema;

    @BeforeAll
    static void setup() throws IOException {

        File file = new File("json-schemas/student.json");
        File file1 = new File("json-schemas/course.json");
        String model = "[" + Files.readString(file.toPath()) + ",\n" + Files.readString(file1.toPath()) + "]";

        Gson gson = new Gson();
        schema = gson.fromJson(model, JsonSchemaList.class);
    }


    @Test
    void ConversionTest() throws ClassNotFoundException {

        JsonSchemaToClassConverter converter = new JsonSchemaToClassConverter(
                ORMTable.getOrmAnnotations(Framework.SPRING_DATA)
        );

        ClassMetadataList classMetadata = converter.convertMany(schema);

        MfClassGenerator generator = new MfClassGenerator(classMetadata, schema);
        var classes = generator.generate();
    }

}
