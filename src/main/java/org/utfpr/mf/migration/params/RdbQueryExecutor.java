package org.utfpr.mf.migration.params;

import lombok.AllArgsConstructor;
import org.utfpr.mf.interfaces.IQuery;
import org.utfpr.mf.interfaces.IQueryExecutor;
import org.utfpr.mf.model.RdbQuery;
import org.utfpr.mf.tools.QueryResult;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

public class RdbQueryExecutor implements IQueryExecutor<Connection, QueryResult> {

    private int callCount = 0;
    private List<RdbQuery> queries;
    private int timesEach;
    private Connection connection;

    public RdbQueryExecutor(List<RdbQuery> queries, int timesEach, Connection connection) {
        this.queries = queries;
        this.timesEach = timesEach;
        this.connection = connection;
    }

    @Override
    public int getTimesEach() {
        return timesEach;
    }

    @Override
    public QueryResult executeActual() {
        return queries.get(callCount).execute(connection);
    }

    @Override
    public QueryResult executeAndNext() {
        return queries.get(callCount++).execute(connection);
    }

    @Override
    public long executeAndGetTime() {
        return queries.get(callCount).cronExecute(connection);
    }

    @Override
    public boolean hasNext() {
        return callCount < queries.size();
    }

    @Override
    public IQuery<Connection, QueryResult> next() {
        return queries.get(callCount++);
    }
}
