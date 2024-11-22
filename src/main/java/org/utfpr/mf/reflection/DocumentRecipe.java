package org.utfpr.mf.reflection;

import com.google.gson.internal.LinkedTreeMap;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class DocumentRecipe {

    private String __collection__;
    Map<String, Map<String, ?>> fields;

}
