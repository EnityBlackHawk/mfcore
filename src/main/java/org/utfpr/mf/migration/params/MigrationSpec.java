package org.utfpr.mf.migration.params;

import lombok.Builder;
import lombok.Data;
import org.utfpr.mf.annotarion.Optional;
import org.utfpr.mf.metadata.RelationCardinality;
import org.utfpr.mf.prompt.Framework;

import java.util.List;

@Data
@Builder
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

    @Data
    public static class Workload {
        private Integer regularity;
        private String query;
    }
}
