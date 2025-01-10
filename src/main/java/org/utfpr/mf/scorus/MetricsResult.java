package org.utfpr.mf.scorus;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
public class MetricsResult {

    private Map<String, Integer> colExistence;
    private Map<String, Integer> colDepth;
    private Integer globalDepth;
    private Map<String, Integer> docWidth;
    private Map<String, Integer> refLoad;

}
