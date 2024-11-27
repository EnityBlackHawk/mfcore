package org.utfpr.mf.json;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReferenceTo {

    private String targetTable = "";
    private String targetColumn = "";

}
