package org.utfpr.mf.json;

import dev.langchain4j.model.output.structured.Description;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReferenceTo {

    @Description("Table from where this property's value points to")
    private String targetTable = "";
    @Description("Column from where this property's value points to")
    private String targetColumn = "";

}
