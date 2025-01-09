import com.google.gson.Gson;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.utfpr.mf.json.JsonSchemaList;
import org.utfpr.mf.scorus.Metrics;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MetricsTest {

    static JsonSchemaList schema;

    @BeforeAll
    static void setup() throws IOException {

        File file = new File("json-schemas/student.json");
        File file1 = new File("json-schemas/course.json");
        String model = "[" + Files.readString(file.toPath()) + ",\n" + Files.readString(file1.toPath()) + "]";

        Gson gson = new Gson();
        schema = gson.fromJson(model, JsonSchemaList.class);
    }

    @Test
    void colExistence() {
        int result = Metrics.colExistence(schema, "Student");
        assertEquals(1, result);
    }

    @Test
    void colDepth() {
        int result = Metrics.colDepth(schema.get(0));
        assertEquals(2, result);

        result = Metrics.colDepth(schema.get(1));
        assertEquals(0, result);
    }

    @Test
    void globalDepth() {
        int result = Metrics.globalDepth(schema);
        assertEquals(2, result);
    }

    @Test
    void docWidth() {
        int result = Metrics.docWidth(schema.get(0));
        assertEquals(7, result);

        result = Metrics.docWidth(schema.get(1));
        assertEquals(3, result);
    }

}
