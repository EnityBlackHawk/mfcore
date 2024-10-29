package org.utfpr.mf.model;

import com.mongodb.client.AggregateIterable;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.bson.BsonArray;
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
    public String getStringQuery() {
        return new BsonArray(query.stream().map(Bson::toBsonDocument).toList()).toString();
    }

    @Override
    public long cronExecute(MongoConnection mongoConnection) {
        var collection = mongoConnection.getDatabase().getCollection(collectionName);
        long initialTime = System.nanoTime();
        var result = collection.aggregate(query);
        var time = System.nanoTime() - initialTime;
        int count = 0;
        for(var document : result) {
            count++;
        }
        return count == 0 ? -1 : time;
    }
}
