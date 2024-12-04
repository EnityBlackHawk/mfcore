import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiChatModelName;
import dev.langchain4j.service.AiServices;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.utfpr.mf.descriptor.CachePolicy;
import org.utfpr.mf.descriptor.LLMServiceDesc;
import org.utfpr.mf.json.JsonSchema;
import org.utfpr.mf.json.JsonSchemaList;
import org.utfpr.mf.llm.ChatAssistant;
import org.utfpr.mf.llm.LLMService;
import org.utfpr.mf.prompt.Framework;
import org.utfpr.mf.prompt.MigrationPreferences;
import org.utfpr.mf.prompt.PromptData4;

import java.util.List;

import static com.github.javaparser.utils.Utils.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class Prompt4 {


    public final String sqlTables = """
            CREATE TABLE alunos (
                id INT PRIMARY KEY AUTO_INCREMENT,
                name VARCHAR(50)
            );
            CREATE TABLE turma (
                id VARCHAR(10) PRIMARY KEY AUTO_INCREMENT,
                materia VARCHAR(50)
            );
            CRATE TABLE turma_alunos (
                id VARCHAR(10) PRIMARY KEY AUTO_INCREMENT,
                materia_id VARCHAR(10) REFERENCES turma(id),
                aluno_id VARCHAR(10) REFERENCES alunos(id)
            );
            """;


    @Test
    void testGetFirst() {

        PromptData4 promptData4 = new PromptData4(
                sqlTables,
                MigrationPreferences.PREFER_PERFORMANCE,
                true,
                Framework.SPRING_DATA,
                null,
                true,
                List.of(),
                List.of()
        );

        System.out.print("Prompt 1: \n" + promptData4.getFirst());

        // System.out.print("Prompt 2: \n" + PromptData4.getSecond(result.content().text(), null));

        LLMServiceDesc desc = new LLMServiceDesc();
        desc.llm_key = "demo";
        desc.cachePolicy = CachePolicy.NO_CACHE;

        LLMService service = new LLMService(desc);
        var l = service.getJsonSchemaList(promptData4.getFirst());

        System.out.print(l.getExplanation());

        assertNotEquals(0, l.getSchemas().size());

    }
}
