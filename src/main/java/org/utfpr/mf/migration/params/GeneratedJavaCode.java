package org.utfpr.mf.migration.params;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
@AllArgsConstructor
public class GeneratedJavaCode {

    private Map<String, String> code;
    private int tokens_used;

    public String getFullSourceCode() {
        return code.values().stream().reduce("", String::concat);
    }

}
