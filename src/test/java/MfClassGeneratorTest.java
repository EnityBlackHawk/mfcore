import com.github.javaparser.symbolsolver.utils.FileUtils;
import com.google.gson.Gson;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.utfpr.mf.json.JsonSchemaList;
import org.utfpr.mf.reflection.MfClassGenerator;

import java.io.*;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MfClassGeneratorTest {

    private static String model;

    private static final String json = """
            [
              {
                "className": "Student",
                "annotations": ["org.springframework.data.mongodb.core.mapping.Document"],
                "fields": [
                  {
                    "name": "id",
                    "type": "java.lang.String",
                    "annotations": ["org.springframework.data.annotation.Id"]
                  },
                  {
                    "name": "name",
                    "type": "java.lang.String",
                    "annotations": []
                  },
                  {
                    "name": "address",
                    "type": "Address",
                    "annotations": []
                  },
                  {
                    "name" : "course",
                    "type" : "java.lang.String",
                    "annotations" : []
                  }
                ]
              },
              {
                "className": "Address",
                "annotations": [],
                "fields": [
                  {
                    "name": "street",
                    "type": "java.lang.String",
                    "annotations": []
                  },
                  {
                    "name": "city",
                    "type": "java.lang.String",
                    "annotations": []
                  },
                  {
                    "name" : "number",
                    "type": "java.lang.String",
                    "annotations": []
                  }
                ]
              }
            ]
            """;

    @BeforeAll
    static void prepare() throws IOException {
        File file = new File("json-schemas/student.json");
        model = "[" + Files.readString(file.toPath()) + "]";
    }


    @Test
    void Gen() throws ClassNotFoundException {
        Gson gson = new Gson();

        MfClassGenerator generator = new MfClassGenerator(json, gson.fromJson(model, JsonSchemaList.class));
        var result = generator.generate();

        assertEquals("""
                import org.utfpr.mf.annotation.FromRDB;
                
                @Document()
                @lombok.Data()
                public class Student {
                
                    @org.springframework.data.annotation.Id()
                    @FromRDB(type = "string", typeClass = java.lang.String.class, column = "id", table = "Students", isReference = false, isAbstract = false, projection = "*")
                    private java.lang.String id;
                
                    @FromRDB(type = "string", typeClass = java.lang.String.class, column = "name", table = "Students", isReference = false, isAbstract = false, projection = "*")
                    private java.lang.String name;
                
                    @FromRDB(type = "Address", typeClass = Address.class, column = "address_id", table = "Students", isReference = false, isAbstract = false, projection = "*", targetTable = "Address", targetColumn = "id")
                    private Address address;
                
                    @FromRDB(type = "string", typeClass = java.lang.String.class, column = "course_id", table = "Students", isReference = true, isAbstract = false, projection = "*", targetTable = "Courses", targetColumn = "id")
                    private java.lang.String course;
                }
                
                """, result.get("Student"));

    }

}
