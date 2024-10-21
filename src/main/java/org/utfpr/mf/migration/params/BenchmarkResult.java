package org.utfpr.mf.migration.params;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BenchmarkResult {

    private String queryName;
    private long nanoSeconds;

    public double getMilliseconds() {
        return nanoSeconds / Math.pow(10, 6);
    }

}
