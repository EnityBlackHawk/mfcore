package org.utfpr.mf.migration.params;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GeneratedJavaCode {

    private Map<String, String> code;
    private int tokens_used;

    @JsonIgnore
    public String getFullSourceCode() {
        return code.values().stream().reduce("", String::concat);
    }

}
