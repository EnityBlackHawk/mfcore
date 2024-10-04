package org.utfpr.mf.migration.params;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.Nullable;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class Model {
    private String model;
    @Nullable
    private String explanation;
    private int tokens_used;
}
