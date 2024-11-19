package org.utfpr.mf.llm;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiChatModelName;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.service.AiServices;
import org.utfpr.mf.descriptor.CachePolicy;
import org.utfpr.mf.descriptor.LLMServiceDesc;

public class LLMService implements ChatAssistant {

    private ChatLanguageModel chatLanguageModel;
    private ChatAssistant chatAssistant;
    private CachePolicy cachePolicy;

    public LLMService(LLMServiceDesc desc) {

        this(desc.llm_key, desc.model, desc.temp, desc.cachePolicy);

    }

    public LLMService(String llm_key, String model, double temp, CachePolicy cachePolicy) {
        this.cachePolicy = cachePolicy;
        chatLanguageModel = new OpenAiChatModel.OpenAiChatModelBuilder()
                .apiKey(llm_key)
                .modelName(OpenAiChatModelName.GPT_4_O_MINI)
                .maxRetries(1)
                .logRequests(true)
                .logResponses(true)
                //.responseFormat("json_object")
                .temperature(temp)
                .build();
        chatAssistant = AiServices.builder(ChatAssistant.class).chatLanguageModel(chatLanguageModel).build();
    }


    @Override
    public Response<AiMessage> chat(String text) {
        return chatAssistant.chat(text);
    }

    @Override
    public Response<AiMessage> getRelations(String text) {
        return chatAssistant.getRelations(text);
    }

    @Override
    public String chatAsString(String userMessage) {
        return chatAssistant.chatAsString(userMessage);
    }
}
