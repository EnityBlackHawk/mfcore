package org.utfpr.mf.migration.params;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.utfpr.mf.metadata.RelationCardinality;

import java.util.List;

@AllArgsConstructor
@Data
public class MetadataInfo {

    private String sql;
    private List<RelationCardinality> relations;

}
