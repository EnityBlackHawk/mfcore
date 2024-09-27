package org.utfpr.mf.migration.params;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

@AllArgsConstructor
@Data
public class MigrationDatabaseReport {

    private Map<String, Integer> tables_count;
    private Map<String, Class<?>> sources;
}
