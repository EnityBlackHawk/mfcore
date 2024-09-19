package org.mf.langchain.prompt;

import org.mf.langchain.DTO.SpecificationDTO;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public record Query(String query, Integer regularity) {

    public Query(SpecificationDTO.WorkloadDTO workloadDTO) {
        this(workloadDTO.getQuery(), workloadDTO.getRegularity());
    }

    public static List<Query> from(String path) throws IOException {
        List<Query> queries = new ArrayList<>();
        StringBuilder queryBuilder = new StringBuilder();

        for (String line : Files.readAllLines(Paths.get(path))) {
            if (line.trim().equals("---")) {
                queries.add(new Query(queryBuilder.toString(), null));
                queryBuilder = new StringBuilder();
            } else {
                queryBuilder.append(line).append("\n");
            }
        }

        // Add the last query if it exists
        if (!queryBuilder.toString().isEmpty()) {
            queries.add(new Query(queryBuilder.toString(), null));
        }

        return queries;
    }

}
