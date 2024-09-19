package org.mf.langchain.prompt;

import org.mf.langchain.metadata.DbMetadata;

import java.util.Iterator;
import java.util.Objects;

public class PrompBuider implements Iterator<String> {
    private final DbMetadata dbMetadata;
    private final String structureOptions;

    private int callCount = 0;

    public static class StructureOptions {
        public static String PREFER_PERFORMANCE = "has embedded documents, not references";
        public static String PREFER_CONSISTENCY = "has less redundant data";
    }

    public PrompBuider(DbMetadata dbMetadata, String structureOptions) {
        this.dbMetadata = dbMetadata;
        this.structureOptions = structureOptions;
    }

    @Override
    public boolean hasNext() {
        return callCount < 2;
    }

    @Override
    public String next() {
        callCount++;
        switch (callCount) {
            case 1:
            {
                String s = "";
                for (var x : dbMetadata.getTables()) {
                    s = s.concat(x.toString() + "\n");
                }
                return "Generate a MongoDB structure for this relational database: \n" + s +
                        "\n" + "Please consider a structure that " + structureOptions;
            }
            case 2:
                return "Now generate a Spring Data MongoDB classes for that structure: \n" +
                        "- Use Lombok \n" +
                        "- " + ((structureOptions.equals(StructureOptions.PREFER_PERFORMANCE)) ? "Some documents are embedded" : "Use @DBRef annotation");

            default:
                return null;
        }
    }




    @Override
    public String toString() {
        String s = "";
        for (var x : dbMetadata.getTables()) {
            s = s.concat(x.toString() + "\n");
        }
        return "Generate a MongoDB structure for this relational database: \n" + s +
                "Please consider a structure that can " + structureOptions + " and the Spring Data MongoDB classes for this data base please, use Lombok";
    }

    public DbMetadata dbMetadata() {
        return dbMetadata;
    }

    public String structureOptions() {
        return structureOptions;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (PrompBuider) obj;
        return Objects.equals(this.dbMetadata, that.dbMetadata) &&
                Objects.equals(this.structureOptions, that.structureOptions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dbMetadata, structureOptions);
    }

}
