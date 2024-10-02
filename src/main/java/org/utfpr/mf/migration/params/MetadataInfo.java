package org.utfpr.mf.migration.params;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.utfpr.mf.metadata.RelationCardinality;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class MetadataInfo {

    private String sql;
    private List<RelationCardinality> relations;

}
