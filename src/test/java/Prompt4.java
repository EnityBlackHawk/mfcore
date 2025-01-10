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
import org.utfpr.mf.prompt.desc.PromptData4Desc;

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

        var promptDesc = new PromptData4Desc();
        promptDesc.sqlTables = sqlTables;
        promptDesc.migrationPreference = MigrationPreferences.PREFER_PERFORMANCE;
        promptDesc.allowReferences = true;
        promptDesc.framework = Framework.SPRING_DATA;
        promptDesc.cardinalityTable = null;
        promptDesc.useMarkdown = true;
        promptDesc.queryList = List.of();
        promptDesc.customPrompts = List.of();

        PromptData4 promptData4 = new PromptData4(promptDesc);

        System.out.print("Prompt 1: \n" + promptData4.getFirst());

        // System.out.print("Prompt 2: \n" + PromptData4.getSecond(result.content().text(), null));

        LLMServiceDesc desc = new LLMServiceDesc();
        desc.llm_key = "demo";
        desc.cachePolicy = CachePolicy.NO_CACHE;
        desc.logRequest = true;
        desc.logResponses = true;

        LLMService service = new LLMService(desc);
        var l = service.getJsonSchemaList(promptData4.getFirst());

        System.out.print(l.getExplanation());

        assertNotEquals(0, l.getSchemas().size());

    }
}
