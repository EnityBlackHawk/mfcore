package org.utfpr.mf.migration.params;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class Model {
    private String model;
    private int tokens_used;
}
