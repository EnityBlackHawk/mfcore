package org.utfpr.mf.llm;

import dev.langchain4j.model.output.structured.Description;
import lombok.Data;
import org.utfpr.mf.json.JsonSchemaList;

@Data
public class LLMResponseJsonSchema {

    @Description("Logic explanation of why this model was chosen")
    private String explanation;
    @Description("A **List** of JsonSchema objects")
    private JsonSchemaList schemas;

}
