import com.github.javaparser.symbolsolver.utils.FileUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.utfpr.mf.reflection.MfClassGenerator;

import java.io.*;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MfClassGeneratorTest {

    private static String model;

    private static final String json = """
            [
              {
                "className": "Alunos",
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
                    "name": "endereco",
                    "type": "Endereco",
                    "annotations": []
                  },
                  {
                    "name" : "tutor",
                    "type" : "Tutor",
                    "annotations" : []
                  }
                ]
              },
              {
                "className": "Tutor",
                "annotations": [],
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
                  }
                ]
              },
              {
                "className": "Endereco",
                "annotations": [],
                "fields": [
                  {
                    "name": "rua",
                    "type": "java.lang.String",
                    "annotations": []
                  },
                  {
                    "name": "numero",
                    "type": "java.lang.String",
                    "annotations": []
                  },
                  {
                    "name": "cidade",
                    "type": "java.lang.String",
                    "annotations": []
                  }
                ]
              },
              {
                "className": "Turma",
                "annotations": ["org.springframework.data.mongodb.core.mapping.Document"],
                "fields": [
                  {
                    "name": "id",
                    "type": "java.lang.String",
                    "annotations": ["org.springframework.data.annotation.Id"]
                  },
                  {
                    "name": "materia",
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
        model = Files.readString(file.toPath());

    }


    @Test
    void Gen() throws ClassNotFoundException {
        MfClassGenerator generator = new MfClassGenerator(json, model);
        var result = generator.generate();

        assertEquals("""
                import org.springframework.data.mongodb.core.mapping.Document;
                import org.utfpr.mf.annotation.FromRDB;
                
                @Document()
                @lombok.Data()
                public class Alunos {
                
                    @org.springframework.data.annotation.Id()
                    @FromRDB(type = string, typeClass = java.lang.String.class, column = id, table = Alunos)
                    private java.lang.String id;
                
                    @FromRDB(type = string, typeClass = java.lang.String.class, column = name, table = Alunos)
                    private java.lang.String name;
                
                    private Endereco endereco;
                }
                """, result.get("Alunos"));



    }

}
