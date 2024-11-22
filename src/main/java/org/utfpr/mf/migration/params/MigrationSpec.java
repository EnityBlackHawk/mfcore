package org.utfpr.mf.migration.params;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.utfpr.mf.annotation.Optional;
import org.utfpr.mf.metadata.RelationCardinality;
import org.utfpr.mf.prompt.Framework;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MigrationSpec {
    private String name;
    @Optional(overriddenBy = MetadataInfo.class)
    private String data_source;
    private List<Workload> workload;
    private Boolean allow_ref;
    private Boolean prioritize_performance;
    private Framework framework;
    private List<String> custom_prompt;
    private String LLM;
    @Optional(overriddenBy = MetadataInfo.class)
    private List<RelationCardinality> cardinality;

    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    public static class Workload {
        private Integer regularity;
        private String query;
    }
}
