package org.utfpr.mf.descriptor;

import dev.langchain4j.model.openai.OpenAiChatModelName;

public class LLMServiceDesc {

    public String llm_key;
    public String model = OpenAiChatModelName.GPT_4_O_MINI.toString();
    public double temp = 0.5;
    public CachePolicy cachePolicy = CachePolicy.DEFAULT;

}
