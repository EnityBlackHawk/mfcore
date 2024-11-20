package org.utfpr.mf.descriptor;

import dev.langchain4j.model.openai.OpenAiChatModelName;

import java.io.PrintStream;

public class LLMServiceDesc {

    public String llm_key;
    public String model = OpenAiChatModelName.GPT_4_O_MINI.toString();
    public double temp = 0.5;
    public CachePolicy cachePolicy = CachePolicy.DEFAULT;
    public PrintStream printStream = System.out;

}
