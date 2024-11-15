import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiChatModelName;
import dev.langchain4j.service.AiServices;
import org.junit.jupiter.api.Test;
import org.utfpr.mf.llm.ChatAssistant;
import org.utfpr.mf.prompt.Framework;
import org.utfpr.mf.prompt.MigrationPreferences;
import org.utfpr.mf.prompt.PromptData4;

import java.util.List;

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

        var gpt = new OpenAiChatModel.OpenAiChatModelBuilder()
                .apiKey(System.getenv("LLM_KEY"))
                .modelName(OpenAiChatModelName.GPT_4_O_MINI)
                .maxRetries(1)
                .temperature(1d)
                .build();
        var gptAssistant = AiServices.builder(ChatAssistant.class).chatLanguageModel(gpt).build();

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
        var result = gptAssistant.chat(promptData4.getFirst());
        System.out.print("Result 1: \n" + result.content().text());

        System.out.print("Prompt 2: \n" + promptData4.getSecond(result.content().text()));
        var result2 = gptAssistant.chat(promptData4.getSecond(result.content().text()));
        System.out.print("Result 2: \n" + result2.content().text());

    }



}
