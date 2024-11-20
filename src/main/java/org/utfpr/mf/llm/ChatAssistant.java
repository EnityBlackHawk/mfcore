package org.utfpr.mf.llm;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

public interface ChatAssistant {

    //@SystemMessage("Generate Java files with classes and imports. Each file in a Markdown code block. Explain your reasoning")
    //@UserMessage("Using this JSON format: [{collection_name: String, document_example: Object}, ...] \n {{it}}")
    Response<AiMessage> chat (String text);
    @UserMessage("Using this JSON format: [{table_source: String, table_target: String, cardinality: String}, ...] \n {{it}}")
    Response<AiMessage> getRelations(String text);
    String chatAsString(String userMessage);
    @SystemMessage("Answer only a plain JSON without Markdown blocks")
    Response<AiMessage> getJson(String text);
}
