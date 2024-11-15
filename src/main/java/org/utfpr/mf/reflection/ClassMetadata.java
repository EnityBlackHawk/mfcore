package org.utfpr.mf.reflection;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClassMetadata {

    private String className;
    private List<FieldMetadata> fields;

}
