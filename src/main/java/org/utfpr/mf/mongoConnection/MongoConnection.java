package org.utfpr.mf.mongoConnection;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import lombok.Data;
import org.springframework.data.mongodb.core.MongoTemplate;

@Data
public class MongoConnection {

    private MongoClient client;
    private MongoTemplate template;

    public MongoConnection(MongoConnectionCredentials credentials) {

        String uri = credentials.getUsername() != null ? String.format("mongodb://%s:%s@%s:%d/%s",
                credentials.getUsername(),
                credentials.getPassword(),
                credentials.getHost(),
                credentials.getPort(),
                credentials.getDatabase())
                :
                String.format("mongodb://%s:%d/%s",
                        credentials.getHost(),
                        credentials.getPort(),
                        credentials.getDatabase());
        MongoClient mongoClient = MongoClients.create(uri);

        this.client = mongoClient;
        this.template = new MongoTemplate(mongoClient, credentials.getDatabase());
    }

}
