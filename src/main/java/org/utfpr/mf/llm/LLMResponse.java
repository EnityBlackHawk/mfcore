package org.utfpr.mf.llm;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.model.output.FinishReason;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.model.output.TokenUsage;

public class LLMResponse extends Response<AiMessage> {

    public LLMResponse(AiMessage content) {
        super(content);
    }

    public LLMResponse(AiMessage content, TokenUsage tokenUsage, FinishReason finishReason) {
        super(content, tokenUsage, finishReason);
    }


}
