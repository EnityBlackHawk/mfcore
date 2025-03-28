package org.utfpr.mf.migration;

import org.utfpr.mf.metadata.Column;
import org.utfpr.mf.metadata.Relations;
import org.utfpr.mf.metadata.Table;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

public class AcquireMetadataStep2 extends AcquireMetadataStep {

    public AcquireMetadataStep2() {
        super();
    }

    public AcquireMetadataStep2(PrintStream printStream) {
        super(printStream);
    }

    @Override
    protected List<Relations> getRelations(String text) {

        BEGIN("Acquiring relations");
        ArrayList<Relations> relations = new ArrayList<>();

        for(Table t : mdb.getTables()) {
            for(Column c : t.columns()) {
                if(c.isFk()) {
                    assert c.fkInfo() != null;
                    String card = c.isUnique() ? "one-to-one" : "many-to-one";
                    relations.add(new Relations(t.name(), c.fkInfo().pk_tableName(), card));
                }
            }
        }

        return relations;

    }
}
