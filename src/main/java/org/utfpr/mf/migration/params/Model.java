package org.utfpr.mf.migration.params;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.Nullable;
import org.utfpr.mf.json.JsonSchemaList;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class Model {
    private String model;
    @Nullable
    private String explanation;
    private int tokens_used;
    private JsonSchemaList models;

    public Model(String model, String explanation, int tokens_used) {
        this(model, explanation, tokens_used, null);
    }

}
