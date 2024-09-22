package org.utfpr.mf.metadata;

public class Relations {
    public String table_source;
    public String table_target;
    public String cardinality;

    public Relations(String table_source, String table_target, String cardinality){
        this.table_source = table_source;
        this.table_target = table_target;
        this.cardinality = cardinality;
    }

    @Override
    public String toString() {
        return table_source + " " + cardinality + " " + table_target;
    }
}
