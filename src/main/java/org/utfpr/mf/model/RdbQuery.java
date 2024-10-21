package org.utfpr.mf.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.utfpr.mf.interfaces.IQuery;
import org.utfpr.mf.tools.DataImporter;
import org.utfpr.mf.tools.QueryResult;

import java.sql.Connection;

@Data
@AllArgsConstructor
public class RdbQuery implements IQuery<Connection, QueryResult> {

    private String name;
    private String query;


    @Override
    public String getName() {
        return name;
    }

    @Override
    public QueryResult execute(Connection connection) {
        return DataImporter.Companion.runQuery(query, connection, QueryResult.class);
    }

    @Override
    public long cronExecute(Connection connection) {
        return DataImporter.Companion.cronQuery(query, connection);
    }
}
