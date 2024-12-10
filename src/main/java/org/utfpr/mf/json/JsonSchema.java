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
@Description("Customized JSON schema")
public class JsonSchema {

    @Description("Type of the JSON Schema")
    private String type;
    @Description("The name of the object")
    private String title;
    private String description;
    @Description("If this property is a composition of another or this object")
    private Boolean isAbstract = false;
    @Description("Describes the custom string type. Example: Date")
    private String format;
    @Description("[REQUIRED If !isAbstract] Column from RDB")
    private String column;
    @Description("[REQUIRED If !isAbstract] Table from RDB")
    private String table;
    private Boolean reference = false;
    @Description("[REQUIRED IF type = object] Is the properties of the object described by this JSON")
    private HashMap<String, JsonSchema> properties;
    @Description("[REQUIRED when a JOIN is need to fetch the data]")
    private ReferenceTo referenceTo;
    @Description("Describes the column from the target table that will be projected into this property")
    private String projection = "*";

}
