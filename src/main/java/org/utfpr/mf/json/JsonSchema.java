package org.utfpr.mf.json;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import dev.langchain4j.model.output.structured.Description;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Description("Customized JSON Schema describing a single JSON collection")
@JsonIgnoreProperties(ignoreUnknown = true)
public class JsonSchema {

    @Description("Type of the JSON Schema")
    private String type = "";
    @Description("[REQUIRED IF type == object] If this property is the ID of the object - All objects must have an id = true")
    private Boolean isId = false;
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
    @Description("[REQUIRE If type == object] Define the type of the relationship")
    private String relationshipType = "none";
    @Description("[REQUIRED If reference] The name of the collection that this property references")
    private String docReferenceTo;
    @Description("[REQUIRED IF type == object] The properties of the object described by this JSON")
    private HashMap<String, JsonSchema> properties;
    @Description("[REQUIRED when column is a FK] Specifies the table and Column to be joined")
    private Reference referenceTo;
    @Description("[REQUIRED when objects inside an array] Describes how to select the data from the target table")
    private Reference referencedBy;
    @Description("[REQUIRED IF type != object and referenceTo != null] Describes the what column from the target table will be projected into this property")
    private String projection = "*";
    @Description("[REQUIRED IF type == array] Describes the items of the array")
    private JsonSchema items;


    public Boolean getReference() {
        return relationshipType.equals(RelationshipType.REFERENCE) && (docReferenceTo != null && !docReferenceTo.isEmpty());
    }

}
