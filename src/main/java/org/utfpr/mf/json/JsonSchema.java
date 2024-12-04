package org.utfpr.mf.json;

import dev.langchain4j.model.output.structured.Description;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.utfpr.mf.json.JsonType;

import java.util.HashMap;
import java.util.Map;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class JsonSchema {

    private String type;
    private String title;
    private String description;
    private String format;
    @Description("Column from RDB")
    private String column;
    @Description("Table from RDB")
    private String table;
    private Boolean reference = false;
    private HashMap<String, JsonSchema> properties;
    private ReferenceTo referenceTo;

}
