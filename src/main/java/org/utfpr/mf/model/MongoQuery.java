package org.utfpr.mf.model;

import com.mongodb.client.AggregateIterable;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.utfpr.mf.interfaces.IQuery;
import org.utfpr.mf.mongoConnection.MongoConnection;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
public class MongoQuery implements IQuery<MongoConnection, AggregateIterable<Document>> {

    private String name;
    private List<Bson> query = new ArrayList<>();
    private String collectionName;


    public AggregateIterable<Document> execute(MongoConnection connection) {

        var collection = connection.getDatabase().getCollection(collectionName);
        return collection.aggregate(query);
    }

    @Override
    public long cronExecute(MongoConnection mongoConnection) {
        var collection = mongoConnection.getDatabase().getCollection(collectionName);
        long initialTime = System.nanoTime();
        collection.aggregate(query);

        return System.nanoTime() - initialTime;
    }
}
