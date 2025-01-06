package org.utfpr.mf.json;

import dev.langchain4j.model.output.structured.Description;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;

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
    private Boolean isAbstract;
    @Description("Describes the custom string type. Example: Date")
    private String format;
    @Description("[REQUIRED If !isAbstract] Column from RDB")
    private String column;
    @Description("[REQUIRED If !isAbstract] Table from RDB")
    private String table;
    private Boolean reference;
    @Description("[REQUIRED IF type = object] The properties of the object described by this JSON")
    private HashMap<String, JsonSchema> properties;
    @Description("[REQUIRED when a JOIN is need to fetch the data]")
    private Reference referenceTo;
    @Description("[REQUIRED when objects inside an array] Describes how to select the data from the target table")
    private Reference referencedBy;
    @Description("[REQUIRED IF type != object and referenceTo != null] Describes the what column from the target table will be projected into this property")
    private String projection;
    @Description("[REQUIRED IF type = array] Describes the items of the array")
    private JsonSchema items;

}
