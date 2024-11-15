package org.utfpr.mf.reflection;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FieldMetadata {

    private String name;
    private String type;
    private List<String> annotations;

}
