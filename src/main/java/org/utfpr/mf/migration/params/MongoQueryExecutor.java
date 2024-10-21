package org.utfpr.mf.migration.params;

import com.mongodb.client.AggregateIterable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.bson.Document;
import org.utfpr.mf.interfaces.IQueryExecutor;
import org.utfpr.mf.model.MongoQuery;
import org.utfpr.mf.mongoConnection.MongoConnection;

import java.util.ArrayList;
import java.util.List;


@AllArgsConstructor
public class MongoQueryExecutor implements IQueryExecutor<MongoConnection, AggregateIterable<Document>> {

    private int callCount = 0;
    @Getter
    @Setter
    private List<MongoQuery> queries;
    @Getter
    @Setter
    private int timesEach;
    @Getter
    @Setter
    private MongoConnection mongoConnection;


    @Override
    public boolean hasNext() {
        return callCount < queries.size();
    }

    @Override
    public MongoQuery next() {
        return queries.get(callCount++);
    }

    @Override
    public AggregateIterable<Document> executeActual() {
        return queries.get(callCount).execute(mongoConnection);
    }

    @Override
    public AggregateIterable<Document> executeAndNext() {
        var result = executeActual();
        callCount++;
        return result;
    }

    @Override
    public long executeAndGetTime() {
        var query = queries.get(callCount);
        return query.cronExecute(mongoConnection);
    }
}
