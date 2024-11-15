import org.junit.jupiter.api.Test;
import org.utfpr.mf.reflection.MfClassGenerator;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MfClassGeneratorTest {

    private static final String json = """
            [
              {
                "className": "Alunos",
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
                    "name": "turmas",
                    "type": "java.lang.List<Turma>",
                    "annotations": []
                  }
                ]
              },
              {
                "className": "Turma",
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
                  },
                  {
                    "name": "alunos",
                    "type": "java.lang.List<Aluno>",
                    "annotations": []
                  }
                ]
              }
            ]
            """;

    @Test
    void Gen() throws ClassNotFoundException {
        MfClassGenerator generator = new MfClassGenerator(json);
        var result = generator.generate();

        assertEquals(result.get("Turma"), """
                import org.springframework.data.mongodb.core.mapping.Document;
                
                @Document()
                @lombok.Data()
                public class Turma {
                
                    @org.springframework.data.annotation.Id()
                    private java.lang.String id;
                
                    private java.lang.String materia;
                
                    private java.lang.List<Aluno> alunos;
                }
                """);



    }

}
